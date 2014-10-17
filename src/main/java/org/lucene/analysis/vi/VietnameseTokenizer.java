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
    private final CharTermAttribute termAtt;
    private final OffsetAttribute offsetAtt;
    private final TypeAttribute type;

    public VietnameseTokenizer(Reader input) {
        super(input);
        termAtt = addAttribute(CharTermAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);
        type = addAttribute(TypeAttribute.class);
    }

    private final void tokenize(Reader input) {
        final List<TaggedWord> words = Lists.newArrayList();
        try {
            final vn.hus.nlp.tokenizer.Tokenizer tokenizer = TokenizerProvider.getInstance()
                    .getTokenizer();
            final SentenceDetector sentenceDetector = SentenceDetectorFactory.create(IConstants.LANG_VIETNAMESE);
            String[] sentences = sentenceDetector.detectSentences(input);
            for (String s : sentences) {
                tokenizer.tokenize(new StringReader(s));
                for (TaggedWord taggedWord : tokenizer.getResult()) {
                    words.add(taggedWord);
                }
            }
            tokenizer.tokenize(input);
            for (TaggedWord taggedWord : tokenizer.getResult()) {
                words.add(taggedWord);
            }
        } catch (IOException e) {
        }
        taggedWords = words.iterator();
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        if (taggedWords.hasNext()) {
            final TaggedWord word = taggedWords.next();
            termAtt.append(word.getText());
            type.setType(word.getRule().getName());
            final int wordLength = word.getText().length();
            offsetAtt.setOffset(offset, offset + wordLength);
            offset += wordLength;
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
        offset = 0;
        tokenize(input);
    }
}
