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


import com.coccoc.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.analysis.VietnameseConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Vietnamese Tokenizer.
 *
 * @author duydo
 */
public class VietnameseTokenizer extends Tokenizer {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    private final List<Token> pending = new CopyOnWriteArrayList<>();

    private int offset = 0;
    private int pos = 0;

    private final VietnameseTokenizerImpl tokenizer;

    public VietnameseTokenizer(VietnameseConfig config) {
        super();
        tokenizer = new VietnameseTokenizerImpl(config);
    }


    @Override
    public final boolean incrementToken() throws IOException {
        while (pending.size() == 0) {
            tokenize();
            if (pending.size() == 0) {
                return false;
            }
        }
        clearAttributes();
        for (int i = pos; i < pending.size(); i++) {
            pos++;
            final Token token = pending.get(i);
            posIncrAtt.setPositionIncrement(1);
            typeAtt.setType(String.format("<%s>", token.getType()));
            termAtt.copyBuffer(token.getText().toCharArray(), 0, token.getText().length());
            offsetAtt.setOffset(correctOffset(token.getPos()), offset = correctOffset(token.getEndPos()));
            return true;
        }
        return false;
    }

    @Override
    public final void end() throws IOException {
        super.end();
        final int finalOffset = correctOffset(offset);
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        pos = 0;
        offset = 0;
        pending.clear();
    }


    private void tokenize() throws IOException {
        final List<Token> tokens = tokenizer.tokenize(input);
        if (tokens != null) {
            pending.addAll(tokens);
        }
    }
}
