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

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 * Factory for a {@link StopFilter} with Vietnamese stop words, registered as {@code vi_stop}.
 *
 * @author duydo
 */
public class VietnameseStopTokenFilterFactory extends AbstractTokenFilterFactory {

    private final CharArraySet stopWords;

    public VietnameseStopTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(name);
        stopWords = VietnameseStopWords.parse(env, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new StopFilter(tokenStream, stopWords);
    }
}
