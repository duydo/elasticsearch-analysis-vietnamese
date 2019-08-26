package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.apache.lucene.analysis.vi.VietnameseTokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.plugin.analysis.vi.AnalysisVietnamesePlugin;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.Matchers.*;
import static org.apache.lucene.analysis.BaseTokenStreamTestCase.assertTokenStreamContents;

/**
 * Created by duydo on 2/19/17.
 */
public class VietnameseAnalysisTest extends ESTestCase {

    public void testSimpleVietnameseAnalysis() throws IOException {
        TestAnalysis analysis = createTestAnalysis();
        assertNotNull(analysis);

        TokenizerFactory tokenizerFactory = analysis.tokenizer.get("vi_tokenizer");
        assertNotNull(tokenizerFactory);
        assertThat(tokenizerFactory, instanceOf(VietnameseTokenizerFactory.class));

        NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
        assertNotNull(analyzer);
        assertThat(analyzer.analyzer(), instanceOf(VietnameseAnalyzer.class));

        analyzer = analysis.indexAnalyzers.get("my_analyzer");
        assertNotNull(analyzer);
        assertThat(analyzer.analyzer(), instanceOf(CustomAnalyzer.class));
        assertThat(analyzer.analyzer().tokenStream(null, new StringReader("")), instanceOf(VietnameseTokenizer.class));

    }


    public void testVietnameseTokenizer() throws IOException {
        TestAnalysis analysis = createTestAnalysis();
        TokenizerFactory tokenizerFactory = analysis.tokenizer.get("vi_tokenizer");
        assertNotNull(tokenizerFactory);

        Tokenizer tokenizer = tokenizerFactory.create();
        assertNotNull(tokenizer);

        tokenizer.setReader(new StringReader("công nghệ thông tin Việt Nam"));
        assertTokenStreamContents(tokenizer, new String[]{"công nghệ thông tin", "Việt", "Nam"});
    }

    public void testVietnameseAnalyzer() throws IOException {
        TestAnalysis analysis = createTestAnalysis();
        NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
        assertNotNull(analyzer);

        TokenStream ts = analyzer.analyzer().tokenStream("test", "công nghệ thông tin Việt Nam");
        CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        for (String expected : new String[]{"công nghệ thông tin", "việt", "nam"}) {
            assertThat(ts.incrementToken(), equalTo(true));
            assertThat(term.toString(), equalTo(expected));
        }
        assertThat(ts.incrementToken(), equalTo(false));
    }

    public TestAnalysis createTestAnalysis() throws IOException {
        String json = "/org/elasticsearch/index/analysis/vi_analysis.json";
        Settings settings = Settings.builder()
                .loadFromStream(json, VietnameseAnalysisTest.class.getResourceAsStream(json), true)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();
        Settings nodeSettings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir()).build();
        return createTestAnalysis(new Index("test", "_na_"), nodeSettings, settings, new AnalysisVietnamesePlugin());
    }

    public void testTokenOffset() throws IOException {

        TestAnalysis analysis = createTestAnalysis();
        NamedAnalyzer analyzer = analysis.indexAnalyzers.get("vi_analyzer");
        assertNotNull(analyzer);

        TokenStream ts = analyzer.analyzer().tokenStream("test", "Phụ tùng xe Mazda bán tải dưới 7 chỗ: ống dẫn gió tới két làm mát khí nạp- cao su lưu hóa, mới 100%, phục vụ BHBD. Ms:1D0013246A");
        CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
        OffsetAttribute offset = ts.getAttribute(OffsetAttribute.class);
        ts.reset();
        String[] expected = new String[]{"phụ tùng", "xe", "mazda", "bán", "tải", "7", "chỗ", "ống", "dẫn", "gió", "tới", "két", "làm", "mát", "khí", "nạp", "cao su", "lưu hóa", "mới", "100%", "phục vụ", "bhbd", "ms", "1", "d0", "013246", "a"};
        int[] expectedOffset = new int[]{0, 9, 12, 18, 22, 31, 33, 38, 42, 46, 50, 54, 58, 62, 66, 70, 75, 82, 91, 95, 101, 109, 115, 118, 119, 121, 127};

        for (int i = 0; i < expected.length; i++) {
            assertThat(ts.incrementToken(), equalTo(true));
            assertThat(term.toString(), equalTo(expected[i]));
            assertTrue(offset.startOffset() == expectedOffset[i]);
        }
        assertThat(ts.incrementToken(), equalTo(false));
    }
}
