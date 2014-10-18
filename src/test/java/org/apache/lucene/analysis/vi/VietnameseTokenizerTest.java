package org.apache.lucene.analysis.vi;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class VietnameseTokenizerTest {

    @Test
    public void testIncrementToken() throws Exception {
        String source = "công nghệ thông tin Việt Nam";

        VietnameseTokenizer tokenizer = new VietnameseTokenizer(new StringReader(source));
        assertNotNull(tokenizer);
        CharTermAttribute termAtt = tokenizer.getAttribute(CharTermAttribute.class);

        tokenizer.reset();

        assertTrue(tokenizer.incrementToken());
        assertEquals("công nghệ thông tin", termAtt.toString());

        assertTrue(tokenizer.incrementToken());
        assertEquals("Việt Nam", termAtt.toString());

        assertFalse(tokenizer.incrementToken());

        tokenizer.end();
        tokenizer.close();
    }
}