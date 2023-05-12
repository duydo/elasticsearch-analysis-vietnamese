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

  public static final String SPACE = " ";
  public static final String UNDERSCORE = "_";
  public static final String COMMA = ",";
  public static final String DOT = ".";


  private static String dictPath = null;

  private static final class Loader {

    private static final Tokenizer INSTANCE = get();

    private Loader() {
    }

    private static Tokenizer get() {
      return new Tokenizer(dictPath);
    }
  }

  public static Tokenizer getInstance(String dictPath) {
    Tokenizer.dictPath = dictPath;
    return Loader.INSTANCE;
  }

  private Tokenizer(String dictPath) {
    int status = initialize(dictPath);
    if (0 > status) {
      throw new RuntimeException(String.format("Cannot initialize Tokenizer: %s", dictPath));
    }
  }

  public List<Token> segment(String text, TokenizeOption option, boolean keepPunctuation) {
    if (text == null) {
      throw new IllegalArgumentException("text is null");
    }
    long resPointer = segmentPointer(text, false, option.value(), keepPunctuation);
    if (resPointer < 0) {
      throw new RuntimeException("Cannot segment the text");
    }

    final List<Token> tokens = new ArrayList<>();
    // Positions from JNI implementation .cpp file
    int rangesSize = Unsafe.getInt(resPointer + 8 * 2);
    long rangesDataPointer = Unsafe.getLong(resPointer + 8 * 3);
    int tokenSize = 4 * 6;
    for (int i = 0; i < rangesSize; ++i) {
      // Positions of UNSAFE values are calculated from {struct Token} in tokenizer.hpp
      int originalStartPos = Unsafe.getInt(rangesDataPointer + i * tokenSize + 8);
      int originalEndPos = Unsafe.getInt(rangesDataPointer + i * tokenSize + 12);
      int type = Unsafe.getInt(rangesDataPointer + i * tokenSize + 16);
      int segType = Unsafe.getInt(rangesDataPointer + i * tokenSize + 20);

      // Build substring from UNSAFE array of codepoints
      final StringBuilder sb = new StringBuilder();
      for (int j = originalStartPos; j < originalEndPos; ++j) {
        sb.appendCodePoint(text.charAt(j));
      }
      tokens.add(new Token(segType == 1 ? sb.toString().replace(COMMA, DOT) : sb.toString(),
          Token.Type.fromInt(type), Token.SegType.fromInt(segType), originalStartPos, originalEndPos));
    }
    freeMemory(resPointer);
    return tokens;
  }


  //Calls CocCoc lib's segmentPointer function
  public native long segmentPointer(String text, boolean forTransforming, int tokenizeOption, boolean keepPunctuation);

  //Calls CocCoc lib's freeMemory function
  private native void freeMemory(long resPointer);

  //Calls CocCoc lib's initialize function
  private native int initialize(String dictPath);
}
