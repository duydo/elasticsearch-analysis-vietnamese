package org.elasticsearch.plugin.analysis.vi;

import com.coccoc.Tokenizer;

/**
 * Detects whether the CocCoc native tokenizer library is installed, so tests that need it can be skipped.
 */
final class NativeLibrary {

    static final boolean AVAILABLE = isAvailable();

    private static boolean isAvailable() {
        try {
            System.loadLibrary(Tokenizer.LIBRARY_NAME);
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    private NativeLibrary() {
    }
}
