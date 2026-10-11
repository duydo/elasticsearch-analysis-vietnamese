package org.elasticsearch.plugin.analysis.vi;

import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexService.IndexCreationContext;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.IndexVersion;
import org.elasticsearch.index.analysis.AnalysisRegistry;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.indices.analysis.wrappers.SettingsInvocationHandler;
import org.elasticsearch.plugins.scanners.NameToPluginInfo;
import org.elasticsearch.plugins.scanners.NamedComponentReader;
import org.elasticsearch.plugins.scanners.PluginInfo;
import org.elasticsearch.plugins.scanners.StablePluginsRegistry;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;
import org.elasticsearch.xcontent.XContentParser;
import org.elasticsearch.xcontent.XContentParserConfiguration;
import org.elasticsearch.xcontent.json.JsonXContent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the plugin the way Elasticsearch loads a stable plugin: components are registered from
 * {@code named_components.json} and instantiated by {@link AnalysisModule}, which injects the settings.
 */
final class StablePluginTestHelper {

    static final Path NAMED_COMPONENTS = Path.of("src/main/plugin-metadata/named_components.json");

    @SuppressWarnings("unchecked")
    static Map<String, Map<String, String>> namedComponents() throws IOException {
        try (InputStream in = Files.newInputStream(NAMED_COMPONENTS);
             XContentParser parser = JsonXContent.jsonXContent.createParser(XContentParserConfiguration.EMPTY, in)) {
            Map<String, Map<String, String>> result = new HashMap<>();
            parser.map().forEach((extensible, components) -> result.put(extensible, (Map<String, String>) components));
            return result;
        }
    }

    static StablePluginsRegistry registry() throws IOException {
        ClassLoader loader = StablePluginTestHelper.class.getClassLoader();
        Map<String, NameToPluginInfo> extensibles = new HashMap<>();
        namedComponents().forEach((extensible, components) -> {
            Map<String, PluginInfo> infos = new HashMap<>();
            components.forEach((name, className) -> infos.put(name, new PluginInfo(name, className, loader)));
            extensibles.put(extensible, new NameToPluginInfo(infos));
        });
        return new StablePluginsRegistry(new NamedComponentReader(), extensibles);
    }

    static ESTestCase.TestAnalysis createTestAnalysis(Path home, Settings analysisSettings) throws IOException {
        Settings settings = Settings.builder()
            .put(IndexMetadata.SETTING_VERSION_CREATED, IndexVersion.current())
            .put(Environment.PATH_HOME_SETTING.getKey(), home)
            .put(analysisSettings)
            .build();
        IndexSettings indexSettings = IndexSettingsModule.newIndexSettings("test", settings);
        AnalysisRegistry registry = new AnalysisModule(new Environment(settings, null), List.of(), registry()).getAnalysisRegistry();
        return new ESTestCase.TestAnalysis(
            registry.build(IndexCreationContext.CREATE_INDEX, indexSettings),
            registry.buildTokenFilterFactories(indexSettings),
            registry.buildTokenizerFactories(indexSettings),
            registry.buildCharFilterFactories(indexSettings)
        );
    }

    /** Creates the settings proxy Elasticsearch would inject for the given component settings. */
    static VietnameseAnalysisSettings analysisSettings(Settings settings) {
        return SettingsInvocationHandler.create(settings, VietnameseAnalysisSettings.class, null);
    }

    private StablePluginTestHelper() {
    }
}
