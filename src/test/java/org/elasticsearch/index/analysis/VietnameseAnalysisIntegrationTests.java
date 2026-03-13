package org.elasticsearch.index.analysis;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.BeforeClass;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.node.info.PluginsAndModules;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginRuntimeInfo;
import org.elasticsearch.plugin.analysis.vi.AnalysisVietnamesePlugin;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.test.ESIntegTestCase.Scope.TEST;
import static org.elasticsearch.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by duydo on 2/20/17.
 */
@ClusterScope(supportsDedicatedMasters=false, numDataNodes=1, numClientNodes=0)
public class VietnameseAnalysisIntegrationTests extends ESIntegTestCase {

    /**
     * True if the CocCoc native tokenizer library is present on this machine.
     * All tests in this class are skipped when it is absent.
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
        assumeTrue(
            "Requires the CocCoc native library (libcoccoc_tokenizer_jni). See TESTING.md.",
            NATIVE_LIB_AVAILABLE
        );
        super.setUp();
    }
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.singleton(AnalysisVietnamesePlugin.class);
    }

    public void testPluginIsLoaded() throws Exception {
        NodesInfoResponse response = client().admin().cluster().prepareNodesInfo().get();
        for (NodeInfo nodeInfo : response.getNodes()) {
            boolean pluginFound = false;
            for (PluginRuntimeInfo pluginInfo : nodeInfo.getInfo(PluginsAndModules.class).getPluginInfos()) {
                if (pluginInfo.descriptor().getName().equals(AnalysisVietnamesePlugin.class.getName())) {
                    pluginFound = true;
                    break;
                }
            }
            assertThat(pluginFound, is(true));
        }
    }

    public void testVietnameseAnalyzer() throws ExecutionException, InterruptedException {

        AnalyzeAction.Response response = client().admin().indices()
                .prepareAnalyze("công nghệ thông tin Việt Nam").setAnalyzer("vi_analyzer")
                .execute().get();
        String[] expected = {"công nghệ", "thông tin", "việt nam"};
        assertThat(response, notNullValue());
        assertThat(response.getTokens().size(), is(3));
        for (int i = 0; i < expected.length; i++) {
            assertThat(response.getTokens().get(i).getTerm(), is(expected[i]));
        }
    }

    public void testVietnameseAnalyzerInMapping() throws ExecutionException, InterruptedException, IOException {
        createIndex("test");
        ensureGreen("test");
        final XContentBuilder mapping = jsonBuilder()
                .startObject()
                .startObject("properties")
                .startObject("foo")
                .field("type", "text")
                .field("analyzer", "vi_analyzer")
                .endObject()
                .endObject()
                .endObject();
        client().admin().indices().preparePutMapping("test").setSource(mapping).get();
        final XContentBuilder source = jsonBuilder()
                .startObject()
                .field("foo", "công nghệ thông tin Việt Nam")
                .endObject();
        index("test", "1", source);
        refresh();
        SearchResponse response = client().search(
                new SearchRequest("test").source(
                        new SearchSourceBuilder().query(QueryBuilders.matchQuery("foo", "công nghệ thông tin"))
                )
        ).actionGet();
        try {
            assertThat(response.getHits().getTotalHits().value(), is(1L));
        } finally {
            response.decRef();
        }
    }
}
