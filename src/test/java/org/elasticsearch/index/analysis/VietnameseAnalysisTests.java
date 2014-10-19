package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.apache.lucene.analysis.vi.VietnameseTokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.plugin.analysis.vi.AnalysisVietnamesePlugin;
import org.elasticsearch.test.ElasticsearchTestCase;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.Matchers.*;

/**
 * @author duydo
 */
public class VietnameseAnalysisTests extends ElasticsearchTestCase {
    @Test
    public void testDefaultsVietnameseAnalysis() throws IOException {
        AnalysisService analysisService = createAnalysisService();

        NamedAnalyzer analyzer = analysisService.analyzer("vi_analyzer");
        assertThat(analyzer.analyzer(), instanceOf(VietnameseAnalyzer.class));

        analyzer = analysisService.analyzer("my_analyzer");
        assertThat(analyzer.analyzer(), instanceOf(CustomAnalyzer.class));
        assertThat(analyzer.analyzer().tokenStream(null, ""), instanceOf(VietnameseTokenizer.class));

        TokenizerFactory tokenizerFactory = analysisService.tokenizer("vi_tokenizer");
        assertThat(tokenizerFactory, instanceOf(VietnameseTokenizerFactory.class));

        String source = "công nghệ thông tin Việt Nam";
        String[] exptected = new String[]{"công nghệ thông tin", "Việt Nam"};

        Tokenizer tokenizer = tokenizerFactory.create(new StringReader(source));
        assertSimpleTokenStreamOutput(tokenizer, exptected);
    }

    public AnalysisService createAnalysisService() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .loadFromClasspath("org/elasticsearch/index/analysis/vi_analysis.json")
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)),
                new IndicesAnalysisModule())
                .createInjector();
        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new AnalysisVietnamesePlugin().onModule(analysisModule);
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);
        return injector.getInstance(AnalysisService.class);
    }

    public static void assertSimpleTokenStreamOutput(TokenStream stream,
                                                     String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertThat(termAttr, notNullValue());
        int i = 0;
        while (stream.incrementToken()) {
            assertThat(expected.length, greaterThan(i));
            assertThat("expected different term at index " + i, expected[i++], equalTo(termAttr.toString()));
        }
        assertThat("not all tokens produced", i, equalTo(expected.length));
        stream.end();
        stream.close();
    }
}
