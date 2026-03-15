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

package org.elasticsearch.analysis;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESTestCase;

/**
 * Unit tests for {@link VietnameseConfig}.
 *
 * <p>Verifies that all four settings ({@code dict_path}, {@code keep_punctuation},
 * {@code split_url}, {@code split_host}) are parsed correctly and that defaults apply
 * when the settings are absent. No native library is required.
 */
public class VietnameseConfigTest extends ESTestCase {

    public void testDefaultValues() {
        VietnameseConfig config = new VietnameseConfig(Settings.EMPTY);
        assertEquals(VietnameseConfig.DEFAULT_DICT_PATH, config.dictPath);
        assertFalse("keep_punctuation should default to false", config.keepPunctuation);
        assertFalse("split_host should default to false", config.splitHost);
        assertFalse("split_url should default to false", config.splitURL);
    }

    public void testDictPathSetting() {
        Settings settings = Settings.builder()
                .put("dict_path", "/custom/dicts")
                .build();
        VietnameseConfig config = new VietnameseConfig(settings);
        assertEquals("/custom/dicts", config.dictPath);
        // Other settings should keep their defaults
        assertFalse(config.keepPunctuation);
        assertFalse(config.splitHost);
        assertFalse(config.splitURL);
    }

    public void testKeepPunctuationSetting() {
        Settings settings = Settings.builder()
                .put("keep_punctuation", true)
                .build();
        VietnameseConfig config = new VietnameseConfig(settings);
        assertTrue(config.keepPunctuation);
        assertFalse(config.splitHost);
        assertFalse(config.splitURL);
    }

    public void testSplitURLSetting() {
        Settings settings = Settings.builder()
                .put("split_url", true)
                .build();
        VietnameseConfig config = new VietnameseConfig(settings);
        assertTrue(config.splitURL);
        assertFalse(config.splitHost);  // unrelated setting must not be affected
    }

    public void testSplitHostSetting() {
        Settings settings = Settings.builder()
                .put("split_host", true)
                .build();
        VietnameseConfig config = new VietnameseConfig(settings);
        assertTrue(config.splitHost);
        assertFalse(config.splitURL);   // unrelated setting must not be affected
    }

    public void testAllSettingsTogether() {
        Settings settings = Settings.builder()
                .put("dict_path", "/opt/vi/dicts")
                .put("keep_punctuation", true)
                .put("split_url", true)
                .put("split_host", true)
                .build();
        VietnameseConfig config = new VietnameseConfig(settings);
        assertEquals("/opt/vi/dicts", config.dictPath);
        assertTrue(config.keepPunctuation);
        assertTrue(config.splitURL);
        assertTrue(config.splitHost);
    }

    public void testDefaultDictPathConstantIsNonEmpty() {
        assertNotNull(VietnameseConfig.DEFAULT_DICT_PATH);
        assertFalse(VietnameseConfig.DEFAULT_DICT_PATH.isEmpty());
    }
}
