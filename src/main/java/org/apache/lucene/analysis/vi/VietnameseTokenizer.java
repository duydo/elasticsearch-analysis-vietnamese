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
    private int finalOffset = 0;
    private int skippedPositions;


    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    private vn.hus.nlp.tokenizer.Tokenizer tokenizer;
    private SentenceDetector sentenceDetector;

    public VietnameseTokenizer(Reader input) {
        this(input, true, false);
    }

    public VietnameseTokenizer(Reader input, boolean useSentenceDetector, boolean useAmbiguitiesResolved) {
        super(input);
        if (useSentenceDetector) {
            sentenceDetector = SentenceDetectorFactory.create(IConstants.LANG_VIETNAMESE);
        }
        tokenizer = TokenizerProvider.getInstance().getTokenizer();
        tokenizer.setAmbiguitiesResolved(useAmbiguitiesResolved);
    }


    private void tokenize(Reader input) throws IOException {
        if (sentenceDetector == null) {
            tokenizer.tokenize(input);
            taggedWords = tokenizer.getResult().iterator();
        } else {
            final List<TaggedWord> words = new ArrayList<TaggedWord>();
            final String[] sentences = sentenceDetector.detectSentences(input);
            for (String s : sentences) {
                tokenizer.tokenize(new StringReader(s));
                words.addAll(tokenizer.getResult());
            }
            taggedWords = words.iterator();
        }
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        while (taggedWords.hasNext()) {
            final TaggedWord word = taggedWords.next();
            final int length = word.getText().length();
            final int currentOffset = offset;
            offset += length;
            if (accept(word)) {
                posIncrAtt.setPositionIncrement(skippedPositions + 1);
                termAtt.copyBuffer(word.getText().trim().toCharArray(), 0, length);
                offsetAtt.setOffset(correctOffset(currentOffset), finalOffset = correctOffset(offset));
                typeAtt.setType(String.format("<%s>", word.getRule().getName().toUpperCase()));
                return true;
            } else {
                // When we skip non-word characters, we still increment the position increment
                skippedPositions++;
            }
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
        // set final offset
        offsetAtt.setOffset(finalOffset, finalOffset);
        // adjust any skipped tokens
        posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        offset = 0;
        finalOffset = 0;
        skippedPositions = 0;
        tokenize(input);
    }
}
