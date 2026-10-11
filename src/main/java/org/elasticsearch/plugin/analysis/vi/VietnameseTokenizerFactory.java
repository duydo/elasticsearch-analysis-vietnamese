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

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.plugin.Inject;
import org.elasticsearch.plugin.NamedComponent;
import org.elasticsearch.plugin.analysis.TokenizerFactory;
import org.elasticsearch.plugin.analysis.vi.lucene.VietnameseTokenizer;

/**
 * Factory for {@link VietnameseTokenizer}, registered as {@code vi_tokenizer}.
 *
 * @author duydo
 */
@NamedComponent(VietnameseTokenizerFactory.NAME)
public class VietnameseTokenizerFactory implements TokenizerFactory {

    public static final String NAME = "vi_tokenizer";

    private final VietnameseConfig config;

    @Inject
    public VietnameseTokenizerFactory(VietnameseAnalysisSettings settings) {
        this.config = VietnameseConfig.from(settings);
    }

    @Override
    public Tokenizer create() {
        return new VietnameseTokenizer(config);
    }
}
