package com.coccoc;

import org.elasticsearch.test.ESTestCase;

/**
 * Unit tests for {@link Tokenizer}. Uses reflection to avoid triggering
 * native library loading — no native library required.
 */
public class TokenizerTest extends ESTestCase {

    /** SPACE and UNDERSCORE were unused dead code and must not be present. */
    public void testSpaceAndUnderscoreConstantsRemoved() {
        expectThrows(NoSuchFieldException.class, () -> Tokenizer.class.getDeclaredField("SPACE"));
        expectThrows(NoSuchFieldException.class, () -> Tokenizer.class.getDeclaredField("UNDERSCORE"));
    }

    /** The old mutable static dictPath field is replaced by the synchronized singleton pattern. */
    public void testMutableDictPathFieldRemoved() {
        expectThrows(NoSuchFieldException.class, () -> Tokenizer.class.getDeclaredField("dictPath"));
    }
}
