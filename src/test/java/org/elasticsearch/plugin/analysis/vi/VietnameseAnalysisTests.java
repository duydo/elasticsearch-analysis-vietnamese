package org.elasticsearch.plugin.analysis.vi;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexVersion;
import org.elasticsearch.index.analysis.AnalysisTestsHelper;
import org.elasticsearch.index.analysis.CustomAnalyzer;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.plugin.analysis.vi.lucene.VietnameseAnalyzer;
import org.elasticsearch.plugin.analysis.vi.lucene.VietnameseTokenizer;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.StringReader;

import static org.apache.lucene.tests.analysis.BaseTokenStreamTestCase.assertAnalyzesTo;
import static org.apache.lucene.tests.analysis.BaseTokenStreamTestCase.assertTokenStreamContents;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Created by duydo on 2/19/17.
 */
public class VietnameseAnalysisTests extends ESSingleNodeTestCase {

    @BeforeClass
    public static void suppressKnownNoisyLoggers() {
        // ES LogConfigurator (run in the parent @BeforeClass) resets log levels, so we
        // re-apply our suppressions here, AFTER the ES config has been applied.
        Configurator.setLevel("org.elasticsearch.deprecation", Level.ERROR);
        Configurator.setLevel("org.elasticsearch.nativeaccess", Level.ERROR);
        Configurator.setLevel("org.apache.lucene.internal.vectorization", Level.ERROR);
        Configurator.setLevel("org.elasticsearch.index.shard.IndexShard", Level.ERROR);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        assumeTrue("Requires the CocCoc native library (libcoccoc_tokenizer_jni). See TESTING.md.",
            NativeLibrary.AVAILABLE);
    }

    public void testVietnameseAnalysis() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        try {
            NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
            assertNotNull(analyzer);
            assertThat(analyzer.analyzer(), instanceOf(VietnameseAnalyzer.class));

            TokenizerFactory tokenizerFactory = analysis.tokenizer.get("vi_tokenizer");
            assertNotNull(tokenizerFactory);
            assertThat(tokenizerFactory, instanceOf(VietnameseTokenizerFactory.class));

            assertNotNull(analysis.tokenFilter.get("vi_stop"));
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testVietnameseAnalyzer() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        try {
            NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
            assertAnalyzesTo(analyzer, "công nghệ thông tin Việt Nam", new String[]{"công nghệ", "thông tin", "việt nam"});
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testVietnameseAnalyzerRemovesStopWords() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        try {
            NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
            assertAnalyzesTo(analyzer, "công nghệ của Việt Nam", new String[]{"công nghệ", "việt nam"});
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testVietnameseAnalyzerNormalizesToLowerCase() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        try {
            NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
            assertEquals("việt nam", analyzer.normalize("field", "Việt Nam").utf8ToString());
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testCustomVietnameseAnalyzer() throws IOException {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.my_analyzer.tokenizer", "vi_tokenizer")
                .build();
        TestAnalysis analysis = createTestAnalysis(settings);
        try {
            NamedAnalyzer analyzer = analysis.indexAnalyzers.get("my_analyzer");
            assertNotNull(analyzer);
            assertThat(analyzer.analyzer(), instanceOf(CustomAnalyzer.class));
            TokenStream ts = analyzer.analyzer().tokenStream(null, new StringReader(""));
            assertThat(ts, instanceOf(VietnameseTokenizer.class));
            assertTokenStreamContents(ts, new String[0]);
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testVietnameseAnalyzerWithCustomTokenizer() throws IOException {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.vi_analyzer.tokenizer", "my_tokenizer")
                .put("index.analysis.tokenizer.my_tokenizer.type", "vi_tokenizer")
                .build();
        TestAnalysis analysis = createTestAnalysis(settings);
        try {
            NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
            assertAnalyzesTo(analyzer, "Công nghệ thông tin Việt Nam https://duydo.me",
                new String[]{"Công nghệ", "thông tin", "Việt Nam", "https", "duydo", "me"});
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testVietnameseTokenizer() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        try {
            Tokenizer tokenizer = analysis.tokenizer.get("vi_tokenizer").create();
            tokenizer.setReader(new StringReader("công nghệ thông tin Việt Nam"));
            assertTokenStreamContents(tokenizer,
                new String[]{"công nghệ", "thông tin", "Việt Nam"},
                new int[]{0, 10, 20},
                new int[]{9, 19, 28},
                new String[]{"<WORD>", "<WORD>", "<WORD>"},
                new int[]{1, 1, 1},
                28);
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testFinalOffsetIncludesTrailingWhitespace() throws IOException {
        Tokenizer tokenizer = new VietnameseTokenizer(VietnameseConfig.DEFAULT);
        tokenizer.setReader(new StringReader("Việt Nam   "));
        assertTokenStreamContents(tokenizer, new String[]{"Việt Nam"}, new int[]{0}, new int[]{8}, 11);
    }

    public void testOffsetsAreCorrectedThroughCharFilter() throws IOException {
        NormalizeCharMap.Builder map = new NormalizeCharMap.Builder();
        map.add("<b>", "");
        map.add("</b>", "");
        Tokenizer tokenizer = new VietnameseTokenizer(VietnameseConfig.DEFAULT);
        tokenizer.setReader(new MappingCharFilter(map.build(), new StringReader("<b>Việt Nam</b>")));
        assertTokenStreamContents(tokenizer, new String[]{"Việt Nam"}, new int[]{3}, new int[]{15}, 15);
    }

    public void testTokenizerIsReusable() throws IOException {
        Tokenizer tokenizer = new VietnameseTokenizer(VietnameseConfig.DEFAULT);
        tokenizer.setReader(new StringReader("công nghệ"));
        assertTokenStreamContents(tokenizer, new String[]{"công nghệ"});
        tokenizer.setReader(new StringReader("thông tin"));
        assertTokenStreamContents(tokenizer, new String[]{"thông tin"});
    }

    public void testConflictingDictPathIsRejected() {
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class,
            () -> new VietnameseTokenizer(new VietnameseConfig("/another/dict/path", false, false, false)));
        assertTrue(e.getMessage(), e.getMessage().contains("/another/dict/path"));
    }

    public TestAnalysis createTestAnalysis(Settings analysisSettings) throws IOException {
        Settings settings = Settings.builder()
                .put(IndexMetadata.SETTING_VERSION_CREATED, IndexVersion.current())
                .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
                .put(analysisSettings)
                .build();
        return AnalysisTestsHelper.createTestAnalysisFromSettings(settings, new AnalysisVietnamesePlugin());
    }
}
