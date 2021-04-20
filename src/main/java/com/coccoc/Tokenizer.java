package com.coccoc;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrite class com.coccoc.Tokenizer for Elasticsearch integration.
 *
 * @author duydo, CocCoc team
 */
public class Tokenizer {
    public static final String TOKENIZER_SHARED_LIB_NAME = "coccoc_tokenizer_jni";

    static {
        System.loadLibrary(TOKENIZER_SHARED_LIB_NAME);
    }

    public static final String SPACE = " ";
    public static final String UNDERSCORE = "_";
    public static final String COMMA = ",";
    public static final String DOT = ".";


    public enum TokenizeOption {
        NORMAL(0),
        HOST(1),
        URL(2);

        private final int value;

        TokenizeOption(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public Tokenizer(String dictPath) {
        int status = initialize(dictPath);
        if (0 > status) {
            throw new RuntimeException(
                    String.format("Cannot initialize Tokenizer: %s", dictPath)
            );
        }
    }

    public List<Token> segment(String text, TokenizeOption option, boolean keepPunctuation) {
        return segment(text, option, keepPunctuation, false);
    }

    public List<Token> segment(String text, TokenizeOption option, boolean keepPunctuation, boolean forTransforming) {
        return segment(text, forTransforming, option.value(), keepPunctuation);
    }

    private List<Token> segment(String text, boolean forTransforming, int tokenizeOption, boolean keepPunctuation) {
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        }
        long resPointer = segmentPointer(text, forTransforming, tokenizeOption, keepPunctuation);

        final List<Token> res = new ArrayList<>();
        // Positions from JNI implementation .cpp file
        long normalizedStringPointer = Unsafe.UNSAFE.getLong(resPointer + 8);
        int rangesSize = (int) Unsafe.UNSAFE.getLong(resPointer + 8 * 2);
        long rangesDataPointer = Unsafe.UNSAFE.getLong(resPointer + 8 * 3);

        int spacePositionsSize = (int) Unsafe.UNSAFE.getLong(resPointer + 8 * 5);
        long spacePositionsDataPointer = Unsafe.UNSAFE.getLong(resPointer + 8 * 6);
        int[] spacePositions = new int[spacePositionsSize + 1];
        for (int i = 0; i < spacePositionsSize; ++i) {
            spacePositions[i] = Unsafe.UNSAFE.getInt(spacePositionsDataPointer + i * 4);
        }
        spacePositions[spacePositionsSize] = -1;

        int tokenSize = 4 * 6;
        for (int i = 0, spacePos = 0; i < rangesSize; ++i) {
            // Positions of UNSAFE values are calculated from {struct Token} in tokenizer.hpp
            int startPos = Unsafe.UNSAFE.getInt(rangesDataPointer + i * tokenSize);
            int endPos = Unsafe.UNSAFE.getInt(rangesDataPointer + i * tokenSize + 4);
            int originalStartPos = Unsafe.UNSAFE.getInt(rangesDataPointer + i * tokenSize + 8);
            int originalEndPos = Unsafe.UNSAFE.getInt(rangesDataPointer + i * tokenSize + 12);
            int type = Unsafe.UNSAFE.getInt(rangesDataPointer + i * tokenSize + 16);
            int segType = Unsafe.UNSAFE.getInt(rangesDataPointer + i * tokenSize + 20);

            // Build substring from UNSAFE array of codepoints
            // TODO: Is there a faster way than using StringBuilder?
            final StringBuilder sb = new StringBuilder();
            for (int j = startPos; j < endPos; ++j) {
                if (j == spacePositions[spacePos]) {
                    sb.append(forTransforming ? UNDERSCORE : SPACE);
                    spacePos++;
                }
                sb.appendCodePoint(Unsafe.UNSAFE.getInt(normalizedStringPointer + j * 4));
            }
            res.add(new Token(segType == 1 ? sb.toString().replace(COMMA, DOT) : sb.toString(),
                    Token.Type.fromInt(type), Token.SegType.fromInt(segType), originalStartPos, originalEndPos));
        }
        if (forTransforming && tokenizeOption == TokenizeOption.NORMAL.value()) {
            res.add(Token.FULL_STOP);
        }
        freeMemory(resPointer);
        return res;
    }


    //Calls CocCoc lib's segmentPointer function
    public native long segmentPointer(String text, boolean forTransforming, int tokenizeOption, boolean keepPunctuation);

    //Calls CocCoc lib's freeMemory function
    private native void freeMemory(long resPointer);

    //Calls CocCoc lib's initialize function
    private native int initialize(String dictPath);
}
