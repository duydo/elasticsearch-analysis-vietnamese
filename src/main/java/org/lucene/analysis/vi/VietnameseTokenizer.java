package org.lucene.analysis.vi;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.common.Preconditions;
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
import java.util.Properties;


/**
 * @author duydo
 */
public class VietnameseTokenizer extends Tokenizer {
    public static final int DEFAULT_BUFFER_SIZE = 1024;


    private int start = 0;
    private int end = 0;
    private int finalOffset = 0;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute type = addAttribute(TypeAttribute.class);
    private Iterator<TaggedWord> tokens;

    public VietnameseTokenizer(Reader input) {
        this(input, DEFAULT_BUFFER_SIZE);
    }

    public VietnameseTokenizer(Reader input, int bufferSize) {
        super(input);
        Preconditions.checkArgument(bufferSize > 0);
        termAtt.resizeBuffer(bufferSize);
        generateVietnameseTokens(input);
    }

    public void generateVietnameseTokens(Reader input) {
        final List<TaggedWord> words = Lists.newArrayList();
        try {
            Properties tokenizerProperties = getTokenizerProperties();
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
            e.printStackTrace();
        }
        tokens = words.iterator();
    }

    private Properties getTokenizerProperties() throws IOException {
        final Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/vi-tokenizer.properties"));
//        Enumeration e = properties.propertyNames();
//        final Properties tokenizerProperties = new Properties();
//        for (; e.hasMoreElements(); ) {
//            final Object key = e.nextElement();
//            final String value = String.valueOf(properties.get(key));
//            String path = getClass().getResource(value).getPath();
//            if (path != null && path.startsWith("file:")) {
//                path = path.substring(5);
//            }
//            tokenizerProperties.put(key, path);
//        }
//        return tokenizerProperties;
        return properties;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (tokens.hasNext()) {
            clearAttributes();
            final TaggedWord word = tokens.next();
            termAtt.append(word.getText());
            type.setType(word.getRule().getName());
            end = start + word.getText().length();
            offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
            start = end;
            return true;
        }
        return false;
    }

    @Override
    public final void end() throws IOException {
        super.end();
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        finalOffset = 0;
        start = 0;
        end = 0;
//        generateVietnameseTokens(input);
    }
}
