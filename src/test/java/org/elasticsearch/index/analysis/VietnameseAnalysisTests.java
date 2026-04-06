package org.elasticsearch.index.analysis;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.BeforeClass;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.apache.lucene.analysis.vi.VietnameseTokenizer;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexVersion;
import org.elasticsearch.plugin.analysis.vi.AnalysisVietnamesePlugin;
import org.elasticsearch.test.ESSingleNodeTestCase;

import java.io.IOException;
import java.io.StringReader;

import static org.apache.lucene.tests.analysis.BaseTokenStreamTestCase.assertTokenStreamContents;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Created by duydo on 2/19/17.
 */
public class VietnameseAnalysisTests extends ESSingleNodeTestCase {

    /**
     * True if the CocCoc native tokenizer library is present on this machine.
     * Tests that exercise actual Vietnamese tokenization are skipped when it is absent.
     */
    private static final boolean NATIVE_LIB_AVAILABLE;
    static {
        boolean available;
        try {
            System.loadLibrary("coccoc_tokenizer_jni");
            available = true;
        } catch (UnsatisfiedLinkError ignored) {
            available = false;
        }
        NATIVE_LIB_AVAILABLE = available;
    }

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
        assumeTrue(
            "Requires the CocCoc native library (libcoccoc_tokenizer_jni). See TESTING.md.",
            NATIVE_LIB_AVAILABLE
        );
    }

    public void testVietnameseAnalysis() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        try {
            assertNotNull(analysis);

            NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
            assertNotNull(analyzer);
            assertThat(analyzer.analyzer(), instanceOf(VietnameseAnalyzer.class));

            TokenizerFactory tokenizerFactory = analysis.tokenizer.get("vi_tokenizer");
            assertNotNull(tokenizerFactory);
            assertThat(tokenizerFactory, instanceOf(VietnameseTokenizerFactory.class));
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testVietnameseAnalyzer() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        try {
            NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
            assertNotNull(analyzer);

            TokenStream ts = analyzer.analyzer().tokenStream("test", "công nghệ thông tin Việt Nam");
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            for (String expected : new String[]{"công nghệ", "thông tin", "việt nam"}) {
                assertThat(ts.incrementToken(), equalTo(true));
                assertThat(term.toString(), equalTo(expected));
            }
            assertThat(ts.incrementToken(), equalTo(false));
            ts.end();
            ts.close();
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
            ts.reset();
            ts.end();
            ts.close();
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
            assertNotNull(analyzer);
            TokenStream ts = analyzer.analyzer().tokenStream("test", "Công nghệ thông tin Việt Nam https://duydo.me");
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            for (String expected : new String[]{"Công nghệ", "thông tin", "Việt Nam", "https", "duydo", "me"}) {
                assertThat(ts.incrementToken(), equalTo(true));
                assertThat(term.toString(), equalTo(expected));
            }
            assertThat(ts.incrementToken(), equalTo(false));
            ts.end();
            ts.close();
        } finally {
            analysis.indexAnalyzers.close();
        }
    }

    public void testVietnameseTokenizer() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        try {
            TokenizerFactory tokenizerFactory = analysis.tokenizer.get("vi_tokenizer");
            assertNotNull(tokenizerFactory);

            Tokenizer tokenizer = tokenizerFactory.create();
            assertNotNull(tokenizer);

            tokenizer.setReader(new StringReader("công nghệ thông tin Việt Nam"));
            assertTokenStreamContents(tokenizer, new String[]{"công nghệ", "thông tin", "Việt Nam"});
        } finally {
            analysis.indexAnalyzers.close();
        }
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
