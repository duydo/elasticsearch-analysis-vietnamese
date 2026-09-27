package com.coccoc;

/**
 * Reads primitive values from native memory owned by the CocCoc JNI library.
 *
 * <p>Two implementations exist: {@link UnsafeNativeMemory} for JDK 21, where the Foreign Function &amp; Memory
 * API is still a preview feature, and {@code FfmNativeMemory} (compiled separately with {@code --release 22})
 * for JDK 22+, where {@code sun.misc.Unsafe} memory access is deprecated for removal.
 */
interface NativeMemory {

    int getInt(long address);

    long getLong(long address);

    static NativeMemory create() {
        if (Runtime.version().feature() >= 22) {
            try {
                return (NativeMemory) Class.forName("com.coccoc.FfmNativeMemory").getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException | LinkageError e) {
                // The plugin was built with JDK 21, so the FFM implementation is not packaged.
            }
        }
        return new UnsafeNativeMemory();
    }
}
