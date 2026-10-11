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

import org.elasticsearch.plugin.settings.AnalysisSettings;
import org.elasticsearch.plugin.settings.BooleanSetting;
import org.elasticsearch.plugin.settings.ListSetting;
import org.elasticsearch.plugin.settings.StringSetting;

import java.util.List;

/**
 * Settings of {@code vi_tokenizer}, {@code vi_analyzer} and {@code vi_stop}. Elasticsearch implements this interface
 * and injects it into the factories' {@code @Inject} constructors.
 */
@AnalysisSettings
public interface VietnameseAnalysisSettings {

    /** Directory containing the CocCoc tokenizer dictionaries. */
    @StringSetting(path = "dict_path", defaultValue = VietnameseConfig.DEFAULT_DICT_PATH)
    String dictPath();

    /** Whether punctuation tokens are emitted. */
    @BooleanSetting(path = "keep_punctuation", defaultValue = false)
    boolean keepPunctuation();

    /** Whether host names are kept as whole words. */
    @BooleanSetting(path = "split_host", defaultValue = false)
    boolean splitHost();

    /** Whether words inside domain names are segmented; takes precedence over {@link #splitHost()}. */
    @BooleanSetting(path = "split_url", defaultValue = false)
    boolean splitUrl();

    /** Stop words: {@code _vi_}, {@code _none_} or a list of words. Empty when not configured. */
    @ListSetting(path = "stopwords")
    List<String> stopwords();

    /** Stop words file, relative to the Elasticsearch config directory. Empty when not configured. */
    @StringSetting(path = "stopwords_path", defaultValue = "")
    String stopwordsPath();

    /** Whether stop words are matched case-insensitively. */
    @BooleanSetting(path = "ignore_case", defaultValue = false)
    boolean ignoreCase();
}
