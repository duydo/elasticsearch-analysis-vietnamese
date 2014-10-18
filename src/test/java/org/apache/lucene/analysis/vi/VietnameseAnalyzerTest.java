package org.apache.lucene.analysis.vi;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class VietnameseAnalyzerTest {
    @Test
    public void simpleTest()  throws IOException {
        VietnameseAnalyzer analyzer = new VietnameseAnalyzer(Version.LUCENE_4_9);
        assertNotNull(analyzer);

        TokenStream test = analyzer.tokenStream("test", "công nghệ thông tin Việt Nam");
        assertNotNull(test);

        test.reset();
        CharTermAttribute termAtt = test.addAttribute(CharTermAttribute.class);
        assertTrue(test.incrementToken());
        assertEquals("công nghệ thông tin", termAtt.toString());

        assertTrue(test.incrementToken());
        assertEquals("việt nam", termAtt.toString());

        assertFalse(test.incrementToken());
    }
}