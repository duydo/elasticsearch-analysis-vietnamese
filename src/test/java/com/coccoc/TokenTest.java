package com.coccoc;

import org.elasticsearch.test.ESTestCase;

/**
 * Unit tests for {@link Token}. No native library required.
 */
public class TokenTest extends ESTestCase {

    public void testTypeFromInt() {
        for (Token.Type type : Token.Type.values()) {
            assertSame(type, Token.Type.fromInt(type.ordinal()));
        }
    }

    public void testTypeFromIntOutOfRangeThrowsIllegalArgument() {
        expectThrows(IllegalArgumentException.class, () -> Token.Type.fromInt(-1));
        expectThrows(IllegalArgumentException.class, () -> Token.Type.fromInt(Token.Type.values().length));
    }

    public void testSegTypeFromInt() {
        for (Token.SegType segType : Token.SegType.values()) {
            assertSame(segType, Token.SegType.fromInt(segType.ordinal()));
        }
    }

    public void testSegTypeFromIntOutOfRangeThrowsIllegalArgument() {
        expectThrows(IllegalArgumentException.class, () -> Token.SegType.fromInt(-1));
        expectThrows(IllegalArgumentException.class, () -> Token.SegType.fromInt(Token.SegType.values().length));
    }

    /** Ordinals must match the constants in coccoc-tokenizer's tokenizer/token.hpp. */
    public void testOrdinalsMatchNativeConstants() {
        assertEquals(0, Token.Type.WORD.ordinal());
        assertEquals(1, Token.Type.NUMBER.ordinal());
        assertEquals(2, Token.Type.SPACE.ordinal());
        assertEquals(3, Token.Type.PUNCT.ordinal());
        assertEquals(1, Token.SegType.SKIP_SEG_TYPE.ordinal());
        assertEquals(4, Token.SegType.END_SEG_TYPE.ordinal());
    }
}
