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
import org.elasticsearch.common.settings.Settings;

/**
 * Settings shared by {@code vi_tokenizer} and {@code vi_analyzer}.
 *
 * @param dictPath        directory containing the CocCoc tokenizer dictionaries
 * @param keepPunctuation whether punctuation tokens are emitted
 * @param splitHost       whether host names are split into their parts
 * @param splitUrl        whether URLs are split into their parts (takes precedence over {@code splitHost})
 */
public record VietnameseConfig(String dictPath, boolean keepPunctuation, boolean splitHost, boolean splitUrl) {

    public static final String DEFAULT_DICT_PATH = "/usr/local/share/tokenizer/dicts";

    public static final String DICT_PATH = "dict_path";
    public static final String KEEP_PUNCTUATION = "keep_punctuation";
    public static final String SPLIT_HOST = "split_host";
    public static final String SPLIT_URL = "split_url";

    public static final VietnameseConfig DEFAULT = new VietnameseConfig(DEFAULT_DICT_PATH, false, false, false);

    public static VietnameseConfig fromSettings(Settings settings) {
        return new VietnameseConfig(
            settings.get(DICT_PATH, DEFAULT_DICT_PATH),
            settings.getAsBoolean(KEEP_PUNCTUATION, false),
            settings.getAsBoolean(SPLIT_HOST, false),
            settings.getAsBoolean(SPLIT_URL, false)
        );
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
