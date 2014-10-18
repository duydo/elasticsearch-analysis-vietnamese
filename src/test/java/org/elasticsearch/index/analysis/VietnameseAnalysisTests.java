package org.elasticsearch.index.analysis;

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

import static org.hamcrest.Matchers.instanceOf;

/**
 * @author duydo
 */
public class VietnameseAnalysisTests extends ElasticsearchTestCase {
    @Test
    public void testDefaultsVietnameseAnalysis() throws IOException {
        AnalysisService analysisService = createAnalysisService();

        TokenizerFactory tokenizerFactory = analysisService.tokenizer("vi_tokenizer");
        assertThat(tokenizerFactory, instanceOf(VietnameseTokenizerFactory.class));

        NamedAnalyzer analyzer = analysisService.analyzer("my_analyzer");
        assertThat(analyzer.analyzer(), instanceOf(CustomAnalyzer.class));
        assertThat(analyzer.analyzer().tokenStream(null, new StringReader("")), instanceOf(VietnameseTokenizer.class));
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
}
