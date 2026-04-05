package com.coccoc;

import com.coccoc.Token.SegType;
import com.coccoc.Token.Type;
import java.util.Arrays;
import java.util.List;
import org.elasticsearch.test.ESTestCase;

/**
 * Unit tests for {@link Token}. No native library required.
 */
public class TokenTest extends ESTestCase {

    /** Current code throws ArrayIndexOutOfBoundsException — must throw IllegalArgumentException. */
    public void testTypeFromIntNegativeThrowsIllegalArgument() {
        expectThrows(IllegalArgumentException.class, () -> Type.fromInt(-1));
    }

    public void testTypeFromIntOutOfRangeThrowsIllegalArgument() {
        expectThrows(IllegalArgumentException.class, () -> Type.fromInt(Type.values().length));
    }

    public void testSegTypeFromIntNegativeThrowsIllegalArgument() {
        expectThrows(IllegalArgumentException.class, () -> SegType.fromInt(-1));
    }

    public void testSegTypeFromIntOutOfRangeThrowsIllegalArgument() {
        expectThrows(IllegalArgumentException.class, () -> SegType.fromInt(SegType.values().length));
    }

    public void testClonePreservesSplitByDot() {
        Token original = new Token("3.14", Type.NUMBER, SegType.OTHER_SEG_TYPE, true, 0, 4);
        Token cloned = original.clone();
        assertTrue("clone() must preserve the splitByDot field", cloned.isSplitByDot());
    }

    public void testEqualsReflexive() {
        Token t = new Token("xin chào", Type.WORD, SegType.OTHER_SEG_TYPE, 0, 8);
        assertTrue(t.equals(t));
    }

    /** Documents design: equals only checks text + type — position and segType are ignored. */
    public void testEqualsIgnoresPositionAndSegType() {
        Token a = new Token("công nghệ", Type.WORD, SegType.END_SEG_TYPE, 0, 9);
        Token b = new Token("công nghệ", Type.WORD, SegType.URL_SEG_TYPE, 99, 108);
        assertTrue("Same text+type but different pos/segType must be equal", a.equals(b));
        assertEquals("Equal tokens must have equal hashCodes", a.hashCode(), b.hashCode());
    }

    public void testEqualsDifferentTextReturnsFalse() {
        Token a = new Token("foo", Type.WORD, SegType.OTHER_SEG_TYPE, 0, 3);
        Token b = new Token("bar", Type.WORD, SegType.OTHER_SEG_TYPE, 0, 3);
        assertFalse(a.equals(b));
    }

    public void testToStringListReturnsTexts() {
        List<Token> tokens = Arrays.asList(
            new Token("công nghệ", Type.WORD, SegType.OTHER_SEG_TYPE, 0, 9),
            new Token("thông tin", Type.WORD, SegType.OTHER_SEG_TYPE, 10, 19)
        );
        List<String> texts = Token.toStringList(tokens);
        assertEquals(Arrays.asList("công nghệ", "thông tin"), texts);
    }

    public void testToStringListEmpty() {
        List<String> texts = Token.toStringList(Arrays.asList());
        assertTrue("Empty token list must yield empty string list", texts.isEmpty());
    }

    /** end <= 0 triggers the start + text.length() fallback in the constructor. */
    public void testConstructorEndPositionFallback() {
        Token t = new Token("hello", Type.WORD, SegType.OTHER_SEG_TYPE, 5, 0);
        assertEquals(5 + "hello".length(), t.getEndPos());
    }

    public void testTypeCheckBooleans() {
        assertTrue(new Token("a", Type.WORD, SegType.OTHER_SEG_TYPE, 0, 1).isWord());
        assertTrue(new Token("1", Type.NUMBER, SegType.OTHER_SEG_TYPE, 0, 1).isNumber());
        assertTrue(new Token(" ", Type.SPACE, 0, 1).isSpace());
        assertTrue(new Token(".", Type.PUNCT, SegType.OTHER_SEG_TYPE, 0, 1).isPunct());
        assertTrue(new Token("http://x.com", Type.WHOLE_URL, SegType.OTHER_SEG_TYPE, 0, 12).isWholeUrl());
        assertTrue(new Token("x.com", Type.SITE_URL, SegType.OTHER_SEG_TYPE, 0, 5).isSiteUrl());
    }

    public void testStaticConstants() {
        assertEquals(".", Token.FULL_STOP.getText());
        assertTrue(Token.FULL_STOP.isPunct());
        assertEquals(",", Token.COMMA.getText());
        assertTrue(Token.COMMA.isPunct());
        assertEquals(" ", Token.SPACE.getText());
        assertTrue(Token.SPACE.isSpace());
    }

    public void testCloneWithNewText() {
        Token original = new Token("xin", Type.WORD, SegType.END_SEG_TYPE, 0, 3);
        Token cloned = original.cloneWithNewText("xin chào", 8);
        assertEquals("xin chào", cloned.getText());
        assertEquals(Type.WORD, cloned.getType());
        assertEquals(SegType.END_SEG_TYPE, cloned.getSegType());
        assertEquals(0, cloned.getPos());
        assertEquals(8, cloned.getEndPos());
    }
}
