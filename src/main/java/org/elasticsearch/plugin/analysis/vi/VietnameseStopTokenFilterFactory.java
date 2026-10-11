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
import org.elasticsearch.plugin.Inject;
import org.elasticsearch.plugin.NamedComponent;
import org.elasticsearch.plugin.analysis.TokenFilterFactory;

/**
 * Factory for a {@link StopFilter} with Vietnamese stop words, registered as {@code vi_stop}.
 *
 * @author duydo
 */
@NamedComponent(VietnameseStopTokenFilterFactory.NAME)
public class VietnameseStopTokenFilterFactory implements TokenFilterFactory {

    public static final String NAME = "vi_stop";

    private final VietnameseAnalysisSettings settings;
    /** Resolved on first use, see {@link VietnameseStopWords}. */
    private volatile CharArraySet stopWords;

    @Inject
    public VietnameseStopTokenFilterFactory(VietnameseAnalysisSettings settings) {
        this.settings = settings;
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new StopFilter(tokenStream, stopWords());
    }

    private CharArraySet stopWords() {
        CharArraySet words = stopWords;
        if (words == null) {
            // Resolving is idempotent, so a concurrent first use only does redundant work
            words = VietnameseStopWords.resolve(settings);
            stopWords = words;
        }
        return words;
    }
}
