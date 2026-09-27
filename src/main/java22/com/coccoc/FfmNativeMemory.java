package com.coccoc;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * {@link NativeMemory} backed by the Foreign Function &amp; Memory API (final since JDK 22).
 * Loaded reflectively by {@link NativeMemory#create()}, never referenced directly.
 */
final class FfmNativeMemory implements NativeMemory {

    // A single unbounded segment lets us read any address without allocating a segment per read.
    private static final MemorySegment ALL = MemorySegment.NULL.reinterpret(Long.MAX_VALUE);

    @Override
    public int getInt(long address) {
        return ALL.get(ValueLayout.JAVA_INT_UNALIGNED, address);
    }

    @Override
    public long getLong(long address) {
        return ALL.get(ValueLayout.JAVA_LONG_UNALIGNED, address);
    }
}
