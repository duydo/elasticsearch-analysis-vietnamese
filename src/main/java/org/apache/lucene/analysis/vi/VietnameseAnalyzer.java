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

import org.apache.lucene.analysis.*;
import org.elasticsearch.analysis.VietnameseConfig;

import java.io.IOException;

/**
 * {@link Analyzer} for Vietnamese language
 *
 * @author duydo
 */
public class VietnameseAnalyzer extends StopwordAnalyzerBase {

    /**
     * File containing default Vietnamese stopwords.
     */
    public final static String DEFAULT_STOPWORD_FILE = "stopwords.txt";
    /**
     * The comment character in the stopwords file.
     * All lines prefixed with this will be ignored.
     */
    private static final String STOPWORDS_COMMENT = "#";

    /**
     * Returns an unmodifiable instance of the default stop words set.
     *
     * @return default stop words set.
     */
    public static CharArraySet getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }


    /**
     * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer class
     * accesses the static final set the first time.;
     */
    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET;

        static {
            try {
                DEFAULT_STOP_SET = loadStopwordSet(false, VietnameseAnalyzer.class, DEFAULT_STOPWORD_FILE, STOPWORDS_COMMENT);
            } catch (IOException ex) {
                // default set should always be present as it is part of the
                // distribution (JAR)
                throw new RuntimeException("Unable to load default stopword set");
            }
        }
    }


    private final VietnameseConfig config;

    /**
     * Builds an analyzer with the default stop words: {@link #getDefaultStopSet}.
     */
    public VietnameseAnalyzer(VietnameseConfig config) {
        this(config, getDefaultStopSet());
    }

    /**
     * Builds an analyzer with the default stop words
     */
    public VietnameseAnalyzer(VietnameseConfig config, CharArraySet stopWords) {
        super(stopWords);
        this.config = config;
    }


    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new VietnameseTokenizer(config);
        TokenStream result = new StopFilter(source, stopwords);
        result = new LowerCaseFilter(result);
        return new TokenStreamComponents(source, result);
    }
}
