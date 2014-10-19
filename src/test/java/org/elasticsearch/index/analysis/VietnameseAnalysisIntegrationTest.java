/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.elasticsearch.index.analysis;

/**
 * @author duydo
 */

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@ElasticsearchIntegrationTest.ClusterScope(scope = ElasticsearchIntegrationTest.Scope.SUITE)
public class VietnameseAnalysisIntegrationTest extends ElasticsearchIntegrationTest {
    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return ImmutableSettings.builder()
                .put(super.nodeSettings(nodeOrdinal))
                .put("plugins." + PluginsService.LOAD_PLUGIN_FROM_CLASSPATH, true)
                .build();
    }

    @Test
    public void testVietnameseAnalyzer() throws ExecutionException, InterruptedException {
        AnalyzeResponse response = client().admin().indices()
                .prepareAnalyze("công nghệ thông tin Việt Nam").setAnalyzer("vi_analyzer")
                .execute().get();
        String[] expected = {"công nghệ thông tin", "việt nam"};
        assertThat(response, notNullValue());
        assertThat(response.getTokens().size(), is(2));
        for (int i = 0; i < expected.length; i++) {
            assertThat(response.getTokens().get(i).getTerm(), is(expected[i]));
        }
    }

    @Test
    public void testVietnameseAnalyzerInMapping() throws ExecutionException, InterruptedException, IOException {
        createIndex("test");
        ensureGreen("test");
        final XContentBuilder mapping = jsonBuilder().startObject()
                .startObject("type")
                .startObject("properties")
                .startObject("foo")
                .field("type", "string")
                .field("analyzer", "vi_analyzer")
                .endObject()
                .endObject()
                .endObject()
                .endObject();
        client().admin().indices().preparePutMapping("test").setType("type").setSource(mapping).get();
        index("test", "type", "1", "foo", "công nghệ thông tin Việt Nam");
        refresh();
        SearchResponse response = client().prepareSearch("test").setQuery(
                QueryBuilders.matchQuery("foo", "Việt Nam")
        ).execute().actionGet();
        assertThat(response.getHits().getTotalHits(), is(1L));
    }
}