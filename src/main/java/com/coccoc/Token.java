package com.coccoc;

/**
 * A token produced by the CocCoc tokenizer.
 *
 * @param text        token text
 * @param type        token type
 * @param segType     segmentation type
 * @param startOffset start offset (inclusive) in the original text
 * @param endOffset   end offset (exclusive) in the original text
 */
public record Token(String text, Type type, SegType segType, int startOffset, int endOffset) {

    /** Mirrors the token type constants of {@code struct Token} in {@code tokenizer/token.hpp}. */
    public enum Type {
        WORD,
        NUMBER,
        SPACE,
        PUNCT,
        WHOLE_URL,
        SITE_URL;

        private static final Type[] VALUES = values();

        public static Type fromInt(int i) {
            if (i < 0 || i >= VALUES.length) {
                throw new IllegalArgumentException("Invalid Token.Type ordinal: " + i);
            }
            return VALUES[i];
        }
    }

    /** Mirrors the segmentation type constants of {@code struct Token} in {@code tokenizer/token.hpp}. */
    public enum SegType {
        OTHER_SEG_TYPE,
        SKIP_SEG_TYPE,
        URL_SEG_TYPE,
        END_URL_TYPE,
        END_SEG_TYPE;

        private static final SegType[] VALUES = values();

        public static SegType fromInt(int i) {
            if (i < 0 || i >= VALUES.length) {
                throw new IllegalArgumentException("Invalid Token.SegType ordinal: " + i);
            }
            return VALUES[i];
        }
    }
}
