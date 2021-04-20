package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.apache.lucene.analysis.vi.VietnameseTokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugin.analysis.vi.AnalysisVietnamesePlugin;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.io.StringReader;

import static org.apache.lucene.analysis.BaseTokenStreamTestCase.assertTokenStreamContents;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Created by duydo on 2/19/17.
 */
public class VietnameseAnalysisTests extends ESTestCase {

    public void testVietnameseAnalysis() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        assertNotNull(analysis);

        NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
        assertNotNull(analyzer);
        assertThat(analyzer.analyzer(), instanceOf(VietnameseAnalyzer.class));

        TokenizerFactory tokenizerFactory = analysis.tokenizer.get("vi_tokenizer");
        assertNotNull(tokenizerFactory);
        assertThat(tokenizerFactory, instanceOf(VietnameseTokenizerFactory.class));

    }


    public void testVietnameseAnalyzer() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
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
    }


    public void testCustomVietnameseAnalyzer() throws IOException {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.my_analyzer.tokenizer", "vi_tokenizer")
                .build();
        TestAnalysis analysis = createTestAnalysis(settings);

        NamedAnalyzer analyzer = analysis.indexAnalyzers.get("my_analyzer");
        assertNotNull(analyzer);
        assertThat(analyzer.analyzer(), instanceOf(CustomAnalyzer.class));
        assertThat(analyzer.analyzer().tokenStream(null, new StringReader("")), instanceOf(VietnameseTokenizer.class));
    }


    public void testVietnameseAnalyzerWithCustomTokenizer() throws IOException {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.vi_analyzer.tokenizer", "my_tokenizer")
//                .put("index.analysis.analyzer.vi_analyzer.filter", "vi_stop")
                .put("index.analysis.tokenizer.my_tokenizer.type", "vi_tokenizer")
//                .put("index.analysis.tokenizer.my_tokenizer.split_url", true)
                .build();
        TestAnalysis analysis = createTestAnalysis(settings);
        NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
        assertNotNull(analyzer);
        TokenStream ts = analyzer.analyzer().tokenStream("test", "Công nghệ thông tin Việt Nam https://duydo.me");
        CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        for (String expected : new String[]{"công nghệ", "thông tin", "việt nam", "https", "duydo", "me"}) {
            assertThat(ts.incrementToken(), equalTo(true));
            assertThat(term.toString(), equalTo(expected));
        }
        assertThat(ts.incrementToken(), equalTo(false));
    }

    public void testVietnameseTokenizer() throws IOException {
        TestAnalysis analysis = createTestAnalysis(Settings.EMPTY);
        TokenizerFactory tokenizerFactory = analysis.tokenizer.get("vi_tokenizer");
        assertNotNull(tokenizerFactory);

        Tokenizer tokenizer = tokenizerFactory.create();
        assertNotNull(tokenizer);

        tokenizer.setReader(new StringReader("công nghệ thông tin Việt Nam"));
        assertTokenStreamContents(tokenizer, new String[]{"công nghệ", "thông tin", "việt nam"});
    }


    public TestAnalysis createTestAnalysisFromFile() throws IOException {
        String json = "/org/elasticsearch/index/analysis/vi_analysis.json";
        Settings settings = Settings.builder()
                .loadFromStream(json, VietnameseAnalysisTests.class.getResourceAsStream(json), true)
                .build();
        return createTestAnalysis(settings);
    }


    public TestAnalysis createTestAnalysis(Settings analysisSettings) throws IOException {
        Settings settings = Settings.builder()
                .put(IndexMetadata.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
                .put(analysisSettings)
                .build();
        return AnalysisTestsHelper.createTestAnalysisFromSettings(settings, new AnalysisVietnamesePlugin());
    }
}
