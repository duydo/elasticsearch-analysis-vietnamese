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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import vn.hus.nlp.sd.IConstants;
import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.sd.SentenceDetectorFactory;
import vn.hus.nlp.tokenizer.TokenizerProvider;
import vn.hus.nlp.tokenizer.tokens.TaggedWord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Vietnamese Tokenizer.
 *
 * @author duydo
 */
public class VietnameseTokenizer extends Tokenizer {

    private Iterator<TaggedWord> taggedWords;

    private int offset = 0;
    private int skippedPositions;


    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    private vn.hus.nlp.tokenizer.Tokenizer tokenizer;
    private SentenceDetector sentenceDetector;

    private boolean sentenceDetectorEnabled;
    private boolean ambiguitiesResolved;

    public VietnameseTokenizer() {
        this(true, false);
    }

    public VietnameseTokenizer(boolean sentenceDetectorEnabled, boolean ambiguitiesResolved) {
        super();
        this.sentenceDetectorEnabled = sentenceDetectorEnabled;
        this.ambiguitiesResolved = ambiguitiesResolved;

        if (this.sentenceDetectorEnabled) {
            sentenceDetector = SentenceDetectorFactory.create(IConstants.LANG_VIETNAMESE);
        }
        tokenizer = AccessController.doPrivileged(new PrivilegedAction<vn.hus.nlp.tokenizer.Tokenizer>() {
            @Override
            public vn.hus.nlp.tokenizer.Tokenizer run() {
                return TokenizerProvider.getInstance().getTokenizer();
            }
        });
        tokenizer.setAmbiguitiesResolved(ambiguitiesResolved);
    }

    private void tokenize(Reader input) throws IOException {
        if (isSentenceDetectorEnabled()) {
            final List<TaggedWord> words = new ArrayList<TaggedWord>();
            final String[] sentences = sentenceDetector.detectSentences(input);
            for (String s : sentences) {
                tokenizer.tokenize(new StringReader(s));
                words.addAll(tokenizer.getResult());
            }
            taggedWords = words.iterator();
        } else {
            tokenizer.tokenize(input);
            taggedWords = tokenizer.getResult().iterator();
        }
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        while (taggedWords.hasNext()) {
            final TaggedWord word = taggedWords.next();
            if (accept(word)) {
                final char[] chars = word.getText().trim().toCharArray();
                termAtt.copyBuffer(chars, 0, chars.length);
                typeAtt.setType(word.getRule().getName());
                posIncrAtt.setPositionIncrement(skippedPositions + 1);
                offsetAtt.setOffset(correctOffset(offset), offset = correctOffset(offset + termAtt.length()));
                offset++;
                return true;
            }
            ++skippedPositions;
        }
        return false;
    }

    /**
     * Only accept the word characters.
     */
    private final boolean accept(TaggedWord word) {
        final String token = word.getText();
        if (token.length() == 1) {
            return Character.isLetterOrDigit(token.charAt(0));
        }
        return true;
    }

    @Override
    public final void end() throws IOException {
        super.end();
        offsetAtt.setOffset(offset, offset);
        posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        offset = 0;
        skippedPositions = 0;
        tokenize(input);
    }

    public boolean isSentenceDetectorEnabled() {
        return sentenceDetectorEnabled;
    }

    public boolean isAmbiguitiesResolved() {
        return ambiguitiesResolved;
    }
}
