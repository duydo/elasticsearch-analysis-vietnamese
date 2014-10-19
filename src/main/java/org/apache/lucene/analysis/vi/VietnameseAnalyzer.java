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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.lucene.Lucene;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * @author duydo
 */
public class VietnameseAnalyzer extends StopwordAnalyzerBase {

    public static final CharArraySet VIETNAMESE_STOP_WORDS_SET;

    static {
        final List<String> stopWords = Arrays.asList(
                "bị", "bởi", "cả", "các", "cái", "cần", "càng", "chỉ", "chiếc", "cho", "chứ", "chưa", "chuyện",
                "có", "có thể", "cứ", "của", "cùng", "cũng", "đã", "đang", "đây", "để", "đến nỗi", "đều", "điều",
                "do", "đó", "được", "dưới", "gì", "khi", "không", "là", "lại", "lên", "lúc", "mà", "mỗi", "một cách",
                "này", "nên", "nếu", "ngay", "nhiều", "như", "nhưng", "những", "nơi", "nữa", "phải", "qua", "ra",
                "rằng", "rằng", "rất", "rất", "rồi", "sau", "sẽ", "so", "sự", "tại", "theo", "thì", "trên", "trước",
                "từ", "từng", "và", "vẫn", "vào", "vậy", "vì", "việc", "với", "vừa"
        );
        final CharArraySet stopSet = new CharArraySet(Lucene.VERSION, stopWords, false);
        VIETNAMESE_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }


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
     * accesses the static final set the first time.
     */
    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET = VIETNAMESE_STOP_WORDS_SET;
    }

    /**
     * Builds an analyzer with the default stop words: {@link #getDefaultStopSet}.
     */
    public VietnameseAnalyzer(Version matchVersion) {
        this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param matchVersion lucene compatibility version
     * @param stopWords    a stopWord set
     */
    public VietnameseAnalyzer(Version matchVersion, CharArraySet stopWords) {
        super(matchVersion, stopWords);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final Tokenizer tokenizer = new VietnameseTokenizer(reader);
        TokenStream tokenStream = new LowerCaseFilter(matchVersion, tokenizer);
        tokenStream = new StopFilter(matchVersion, tokenStream, stopwords);
        return new TokenStreamComponents(tokenizer, tokenStream);
    }
}
