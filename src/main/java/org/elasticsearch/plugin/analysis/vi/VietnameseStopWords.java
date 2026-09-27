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
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.Analysis;
import org.elasticsearch.plugin.analysis.vi.lucene.VietnameseAnalyzer;

import java.util.Map;
import java.util.Set;

/**
 * Parses the {@code stopwords}, {@code stopwords_path} and {@code ignore_case} settings shared by
 * {@code vi_analyzer} and {@code vi_stop}. Besides {@code _none_} and explicit word lists, the named list
 * {@code _vi_} (alias {@code _vietnamese_}) refers to the built-in Vietnamese stop words.
 */
final class VietnameseStopWords {

    private static final Map<String, Set<?>> NAMED_STOP_WORDS = Map.of(
        "_vi_", VietnameseAnalyzer.getDefaultStopSet(),
        "_vietnamese_", VietnameseAnalyzer.getDefaultStopSet()
    );

    static CharArraySet parse(Environment env, Settings settings) {
        boolean ignoreCase = settings.getAsBoolean("ignore_case", false);
        CharArraySet stopWords = Analysis.parseWords(env, settings, "stopwords", VietnameseAnalyzer.getDefaultStopSet(),
            NAMED_STOP_WORDS, ignoreCase);
        // Analysis.parseWords returns the (case-sensitive) default set as is when no stop words are configured
        return ignoreCase ? CharArraySet.unmodifiableSet(new CharArraySet(stopWords, true)) : stopWords;
    }

    private VietnameseStopWords() {
    }
}
