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

package org.elasticsearch.plugin.analysis.vi.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.elasticsearch.plugin.analysis.vi.VietnameseConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * {@link Analyzer} for Vietnamese: {@link VietnameseTokenizer}, then {@link LowerCaseFilter} and {@link StopFilter}.
 *
 * @author duydo
 */
public class VietnameseAnalyzer extends StopwordAnalyzerBase {

    /** File containing default Vietnamese stop words, relative to this class. */
    public static final String DEFAULT_STOPWORDS_FILE = "stopwords.txt";

    /** Returns an unmodifiable instance of the default stop words set. */
    public static CharArraySet getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    /** Lazily loads the default stop set the first time it is accessed. */
    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET;

        static {
            try (InputStream in = IOUtils.requireResourceNonNull(
                    VietnameseAnalyzer.class.getResourceAsStream(DEFAULT_STOPWORDS_FILE), DEFAULT_STOPWORDS_FILE)) {
                DEFAULT_STOP_SET = CharArraySet.unmodifiableSet(
                    WordlistLoader.getWordSet(IOUtils.getDecodingReader(in, StandardCharsets.UTF_8), "#"));
            } catch (IOException e) {
                // The default set is bundled in the plugin jar, so this should never happen
                throw new UncheckedIOException("Unable to load default stop word set", e);
            }
        }
    }

    private final VietnameseConfig config;

    /** Builds an analyzer with the default stop words: {@link #getDefaultStopSet}. */
    public VietnameseAnalyzer(VietnameseConfig config) {
        this(config, getDefaultStopSet());
    }

    public VietnameseAnalyzer(VietnameseConfig config, CharArraySet stopWords) {
        super(stopWords);
        this.config = config;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new VietnameseTokenizer(config);
        TokenStream result = new LowerCaseFilter(source);
        result = new StopFilter(result, stopwords);
        return new TokenStreamComponents(source, result);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }
}
