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

package org.elasticsearch.plugin.analysis.vi;


import com.google.common.collect.ImmutableList;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.VietnameseAnalysisBinderProcessor;
import org.elasticsearch.indices.analysis.VietnameseIndicesAnalysisModule;
import org.elasticsearch.plugins.Plugin;

import java.util.Collection;
import java.util.Collections;


/**
 * @author duydo
 */
public class AnalysisVietnamesePlugin extends Plugin {
    @Override
    public String name() {
        return "elasticsearch-analysis-vietnamese";
    }

    @Override
    public String description() {
        return "Elasticsearch Vietnamese Analysis Plugin";
    }

    @Override
    public Collection<Module> nodeModules() {
        return Collections.<Module>singletonList(new VietnameseIndicesAnalysisModule());
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new VietnameseAnalysisBinderProcessor());
    }
}
