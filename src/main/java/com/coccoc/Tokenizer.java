package com.coccoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Java binding for the CocCoc C++ tokenizer (libcoccoc_tokenizer_jni).
 *
 * <p>The native tokenizer is a process-wide singleton that can only be initialized with one dictionary path,
 * so this class is a singleton too. Its package and native method names must match the JNI symbols
 * ({@code Java_com_coccoc_Tokenizer_*}) exported by the native library.
 *
 * @author duydo, CocCoc team
 */
public final class Tokenizer {

    public static final String LIBRARY_NAME = "coccoc_tokenizer_jni";

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

    // Layout of the int64_t[8] array returned by segmentPointer() (java/src/jni/Tokenizer.cpp)
    private static final int RESULT_TOKEN_COUNT = 2 * Long.BYTES;
    private static final int RESULT_TOKENS_POINTER = 3 * Long.BYTES;

    // Layout of struct Token (tokenizer/token.hpp): six int32_t fields
    private static final int TOKEN_SIZE = 6 * Integer.BYTES;
    private static final int TOKEN_ORIGINAL_START = 2 * Integer.BYTES;
    private static final int TOKEN_ORIGINAL_END = 3 * Integer.BYTES;
    private static final int TOKEN_TYPE = 4 * Integer.BYTES;
    private static final int TOKEN_SEG_TYPE = 5 * Integer.BYTES;

    private static Tokenizer instance;
    private static String instanceDictPath;

    private final NativeMemory memory = NativeMemory.create();

    /**
     * Returns the tokenizer, loading the native library and dictionaries on first use.
     *
     * @throws IllegalArgumentException if the tokenizer was already initialized with a different dictionary path
     * @throws IllegalStateException    if the native library cannot be loaded or the dictionaries cannot be read
     */
    public static synchronized Tokenizer getInstance(String dictPath) {
        Objects.requireNonNull(dictPath, "dictPath");
        if (instance == null) {
            loadLibrary();
            instance = new Tokenizer(dictPath);
            instanceDictPath = dictPath;
        } else if (!instanceDictPath.equals(dictPath)) {
            throw new IllegalArgumentException(
                "Tokenizer already initialized with dict_path [" + instanceDictPath + "], cannot use [" + dictPath + "]");
        }
        return instance;
    }

    private static void loadLibrary() {
        try {
            System.loadLibrary(LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            throw new IllegalStateException("Cannot load native library [" + LIBRARY_NAME + "] from java.library.path ["
                + System.getProperty("java.library.path") + "]", e);
        }
    }

    private Tokenizer(String dictPath) {
        if (initialize(dictPath) < 0) {
            throw new IllegalStateException("Cannot initialize tokenizer with dict_path [" + dictPath + "]");
        }
    }

    public List<Token> segment(String text, TokenizeOption option, boolean keepPunctuation) {
        Objects.requireNonNull(text, "text");
        long result = segmentPointer(text, false, option.value(), keepPunctuation);
        if (result < 0) {
            throw new IllegalStateException("Native tokenizer failed to segment text");
        }
        try {
            int count = memory.getInt(result + RESULT_TOKEN_COUNT);
            long tokens = memory.getLong(result + RESULT_TOKENS_POINTER);
            List<Token> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                long token = tokens + (long) i * TOKEN_SIZE;
                int start = memory.getInt(token + TOKEN_ORIGINAL_START);
                int end = memory.getInt(token + TOKEN_ORIGINAL_END);
                Token.SegType segType = Token.SegType.fromInt(memory.getInt(token + TOKEN_SEG_TYPE));
                String tokenText = text.substring(start, end);
                if (segType == Token.SegType.SKIP_SEG_TYPE) {
                    // Decimal separators are normalized to dots, as in the upstream CocCoc Java binding
                    tokenText = tokenText.replace(',', '.');
                }
                list.add(new Token(tokenText, Token.Type.fromInt(memory.getInt(token + TOKEN_TYPE)), segType, start, end));
            }
            return list;
        } finally {
            freeMemory(result);
        }
    }

    private native long segmentPointer(String text, boolean forTransforming, int tokenizeOption, boolean keepPunctuation);

    private native void freeMemory(long resPointer);

    private native int initialize(String dictPath);
}
