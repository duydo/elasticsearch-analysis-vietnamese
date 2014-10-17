package org.lucene.analysis.vi;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.common.collect.Lists;
import vn.hus.nlp.sd.IConstants;
import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.sd.SentenceDetectorFactory;
import vn.hus.nlp.tokenizer.TokenizerProvider;
import vn.hus.nlp.tokenizer.tokens.TaggedWord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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

    private final CharTermAttribute termAttribute;
    private final OffsetAttribute offsetAttribute;
    private final TypeAttribute typeAttribute;

    private final vn.hus.nlp.tokenizer.Tokenizer tokenizer;
    private final SentenceDetector sentenceDetector;

    public VietnameseTokenizer(Reader input) {
        super(input);
        termAttribute = addAttribute(CharTermAttribute.class);
        offsetAttribute = addAttribute(OffsetAttribute.class);
        typeAttribute = addAttribute(TypeAttribute.class);
        tokenizer = TokenizerProvider.getInstance().getTokenizer();
        sentenceDetector = SentenceDetectorFactory.create(IConstants.LANG_VIETNAMESE);
    }

    private void tokenize(Reader input) throws IOException {
        final List<TaggedWord> words = Lists.newArrayList();
        String[] sentences = sentenceDetector.detectSentences(input);
        for (String s : sentences) {
            tokenizer.tokenize(new StringReader(s));
            words.addAll(tokenizer.getResult());
        }
        taggedWords = words.iterator();
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        if (taggedWords.hasNext()) {
            final TaggedWord word = taggedWords.next();
            final int wordLength = word.getText().length();
            termAttribute.copyBuffer(word.getText().trim().toCharArray(), 0, wordLength);
            typeAttribute.setType(word.getRule().getName());
            offsetAttribute.setOffset(correctOffset(offset), finalOffset = correctOffset(offset + wordLength));
            offset += wordLength;
            return true;
        }
        return false;
    }

    @Override
    public final void end() throws IOException {
        super.end();
        offsetAttribute.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        offset = 0;
        finalOffset = 0;
        tokenize(input);
    }
}
