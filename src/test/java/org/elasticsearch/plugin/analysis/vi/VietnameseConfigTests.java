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
import org.elasticsearch.test.ESTestCase;

import static org.elasticsearch.plugin.analysis.vi.StablePluginTestHelper.analysisSettings;

/**
 * Unit tests for {@link VietnameseConfig} built from the injected {@link VietnameseAnalysisSettings}.
 * No native library is required.
 */
public class VietnameseConfigTests extends ESTestCase {

    public void testDefaultValues() {
        assertEquals(VietnameseConfig.DEFAULT, VietnameseConfig.from(analysisSettings(Settings.EMPTY)));
    }

    public void testAllSettings() {
        Settings settings = Settings.builder()
                .put("dict_path", "/opt/vi/dicts")
                .put("keep_punctuation", true)
                .put("split_url", true)
                .put("split_host", true)
                .build();
        assertEquals(new VietnameseConfig("/opt/vi/dicts", true, true, true), VietnameseConfig.from(analysisSettings(settings)));
    }

    public void testEachSettingIsIndependent() {
        assertEquals(new VietnameseConfig("/d", false, false, false),
            VietnameseConfig.from(analysisSettings(Settings.builder().put("dict_path", "/d").build())));
        assertEquals(new VietnameseConfig(VietnameseConfig.DEFAULT_DICT_PATH, true, false, false),
            VietnameseConfig.from(analysisSettings(Settings.builder().put("keep_punctuation", true).build())));
        assertEquals(new VietnameseConfig(VietnameseConfig.DEFAULT_DICT_PATH, false, true, false),
            VietnameseConfig.from(analysisSettings(Settings.builder().put("split_host", true).build())));
        assertEquals(new VietnameseConfig(VietnameseConfig.DEFAULT_DICT_PATH, false, false, true),
            VietnameseConfig.from(analysisSettings(Settings.builder().put("split_url", true).build())));
    }

    public void testTokenizeOption() {
        assertEquals(TokenizeOption.NORMAL, VietnameseConfig.DEFAULT.tokenizeOption());
        assertEquals(TokenizeOption.HOST, new VietnameseConfig("d", false, true, false).tokenizeOption());
        assertEquals(TokenizeOption.URL, new VietnameseConfig("d", false, false, true).tokenizeOption());
        // split_url takes precedence over split_host
        assertEquals(TokenizeOption.URL, new VietnameseConfig("d", false, true, true).tokenizeOption());
    }
}
