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

import com.coccoc.Token;
import com.coccoc.Tokenizer.TokenizeOption;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.plugin.analysis.vi.VietnameseConfig;

import java.io.IOException;
import java.io.StringWriter;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/**
 * {@link Tokenizer} for Vietnamese, backed by the
 * <a href="https://github.com/coccoc/coccoc-tokenizer">CocCoc C++ tokenizer</a>.
 *
 * <p>The native tokenizer segments whole texts, so the input is read fully on the first call to
 * {@link #incrementToken()}.
 *
 * @author duydo
 */
public final class VietnameseTokenizer extends Tokenizer {

    private static final Map<Token.Type, String> TYPE_NAMES = new EnumMap<>(Token.Type.class);

    static {
        for (Token.Type type : Token.Type.values()) {
            TYPE_NAMES.put(type, "<" + type.name() + ">");
        }
    }

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    private final com.coccoc.Tokenizer segmenter;
    private final TokenizeOption option;
    private final boolean keepPunctuation;

    /** Tokens of the current input, or {@code null} until the input has been segmented. */
    private Iterator<Token> tokens;
    /** Length of the current input, used as the final offset. */
    private int inputLength;

    public VietnameseTokenizer(VietnameseConfig config) {
        this.segmenter = com.coccoc.Tokenizer.getInstance(config.dictPath());
        this.option = config.tokenizeOption();
        this.keepPunctuation = config.keepPunctuation();
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        if (tokens == null) {
            segmentInput();
        }
        if (!tokens.hasNext()) {
            return false;
        }
        Token token = tokens.next();
        termAtt.setEmpty().append(token.text());
        offsetAtt.setOffset(correctOffset(token.startOffset()), correctOffset(token.endOffset()));
        typeAtt.setType(TYPE_NAMES.get(token.type()));
        return true;
    }

    private void segmentInput() throws IOException {
        StringWriter writer = new StringWriter();
        input.transferTo(writer);
        String text = writer.toString();
        inputLength = text.length();
        tokens = segmenter.segment(text, option, keepPunctuation).iterator();
    }

    @Override
    public void end() throws IOException {
        super.end();
        int finalOffset = correctOffset(inputLength);
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokens = null;
        inputLength = 0;
    }

    @Override
    public void close() throws IOException {
        super.close();
        tokens = null;
    }
}
