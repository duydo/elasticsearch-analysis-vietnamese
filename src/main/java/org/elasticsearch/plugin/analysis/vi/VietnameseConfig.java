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

import com.coccoc.Tokenizer.TokenizeOption;

/**
 * Tokenizer options shared by {@code vi_tokenizer} and {@code vi_analyzer}.
 *
 * @param dictPath        directory containing the CocCoc tokenizer dictionaries
 * @param keepPunctuation whether punctuation tokens are emitted
 * @param splitHost       whether host names are kept as whole words
 * @param splitUrl        whether words inside domain names are segmented (takes precedence over {@code splitHost})
 */
public record VietnameseConfig(String dictPath, boolean keepPunctuation, boolean splitHost, boolean splitUrl) {

    public static final String DEFAULT_DICT_PATH = "/usr/local/share/tokenizer/dicts";

    public static final VietnameseConfig DEFAULT = new VietnameseConfig(DEFAULT_DICT_PATH, false, false, false);

    public static VietnameseConfig from(VietnameseAnalysisSettings settings) {
        return new VietnameseConfig(settings.dictPath(), settings.keepPunctuation(), settings.splitHost(), settings.splitUrl());
    }

    public TokenizeOption tokenizeOption() {
        if (splitUrl) {
            return TokenizeOption.URL;
        }
        if (splitHost) {
            return TokenizeOption.HOST;
        }
        return TokenizeOption.NORMAL;
    }
}
