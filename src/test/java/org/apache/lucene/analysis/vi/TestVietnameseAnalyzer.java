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

package org.apache.lucene.analysis.vi;

import org.apache.lucene.analysis.CharArraySet;
import org.elasticsearch.analysis.VietnameseConfig;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESTestCase;

import java.util.Arrays;

/**
 * Unit tests for {@link VietnameseAnalyzer}.
 *
 * <p>These tests operate at the pure Lucene level and do NOT require the native CocCoc tokenizer
 * library to be installed. They verify:
 * <ul>
 *   <li>Stop-word set loading via {@code WordlistLoader} (the fix for Lucene 10 / ES 9.x compatibility).</li>
 *   <li>Analyzer construction with default and custom stop-word sets.</li>
 * </ul>
 *
 * <p>Tests that exercise actual Vietnamese tokenization live in
 * {@code VietnameseAnalysisTests} (requires the native library).
 */
public class TestVietnameseAnalyzer extends ESTestCase {

    /**
     * Verifies the {@code WordlistLoader.getWordSet} code path introduced for Lucene 10 compatibility.
     * A failure here typically means {@code stopwords.txt} is missing from the classpath or the
     * resource stream is null.
     */
    public void testDefaultStopSetIsLoaded() {
        CharArraySet stopSet = VietnameseAnalyzer.getDefaultStopSet();
        assertNotNull("Default stop set must not be null", stopSet);
        assertFalse("Default stop set must not be empty", stopSet.isEmpty());
    }

    /** Spot-checks a handful of words known to be present in {@code stopwords.txt}. */
    public void testDefaultStopSetContainsKnownWords() {
        CharArraySet stopSet = VietnameseAnalyzer.getDefaultStopSet();
        // Words present in src/main/resources/org/apache/lucene/analysis/vi/stopwords.txt
        for (String word : new String[]{"bị", "của", "và", "các", "cho"}) {
            assertTrue("Expected stop word '" + word + "' to be in the default set", stopSet.contains(word));
        }
    }

    /** The static holder must return the same instance on every call. */
    public void testDefaultStopSetReturnsSameInstance() {
        assertSame(VietnameseAnalyzer.getDefaultStopSet(), VietnameseAnalyzer.getDefaultStopSet());
    }

    /** Verifies the two-arg constructor accepts a custom stop set without throwing. */
    public void testAnalyzerConstructsWithCustomStopSet() {
        VietnameseConfig config = new VietnameseConfig(Settings.EMPTY);
        CharArraySet customStop = new CharArraySet(Arrays.asList("tôi", "bạn"), true);
        try (VietnameseAnalyzer analyzer = new VietnameseAnalyzer(config, customStop)) {
            assertNotNull(analyzer);
        }
    }

    /** Verifies the single-arg constructor falls back to the default stop set without throwing. */
    public void testAnalyzerConstructsWithDefaultStopSet() {
        VietnameseConfig config = new VietnameseConfig(Settings.EMPTY);
        try (VietnameseAnalyzer analyzer = new VietnameseAnalyzer(config)) {
            assertNotNull(analyzer);
        }
    }

    /** An empty stop-word set is valid; the analyzer must still construct cleanly. */
    public void testAnalyzerConstructsWithEmptyStopSet() {
        VietnameseConfig config = new VietnameseConfig(Settings.EMPTY);
        try (VietnameseAnalyzer analyzer = new VietnameseAnalyzer(config, CharArraySet.EMPTY_SET)) {
            assertNotNull(analyzer);
        }
    }
}
