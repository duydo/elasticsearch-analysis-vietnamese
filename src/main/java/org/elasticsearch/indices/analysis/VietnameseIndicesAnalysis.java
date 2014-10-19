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

package org.elasticsearch.indices.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.vi.VietnameseAnalyzer;
import org.apache.lucene.analysis.vi.VietnameseTokenizer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.analysis.PreBuiltTokenizerFactoryFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;

import java.io.Reader;

/**
 * Registers indices level analysis components so, if not explicitly configured, will be shared among all indices.
 *
 * @author duydo
 */
public class VietnameseIndicesAnalysis extends AbstractComponent {
    @Inject
    public VietnameseIndicesAnalysis(Settings settings, IndicesAnalysisService indicesAnalysisService) {
        super(settings);
        indicesAnalysisService.analyzerProviderFactories().put("vi_analyzer",
                new PreBuiltAnalyzerProviderFactory("vi_analyzer",
                        AnalyzerScope.INDICES, new VietnameseAnalyzer(Lucene.ANALYZER_VERSION)
                )
        );
        indicesAnalysisService.tokenizerFactories().put("vi_tokenizer",
                new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
                    @Override
                    public String name() {
                        return "vi_tokenizer";
                    }

                    @Override
                    public Tokenizer create(Reader reader) {
                        return new VietnameseTokenizer(reader);
                    }
                })
        );
    }
}
