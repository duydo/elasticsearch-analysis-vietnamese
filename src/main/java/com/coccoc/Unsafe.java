package com.coccoc;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Memory access utilities using the Java 21 Foreign Function & Memory (FFM) API
 * as a replacement for the terminally deprecated sun.misc.Unsafe address-based methods.
 */
public class Unsafe {

  public static int getInt(long address) {
    return MemorySegment.ofAddress(address)
        .reinterpret(Integer.BYTES)
        .get(ValueLayout.JAVA_INT_UNALIGNED, 0);
  }

  public static long getLong(long address) {
    return MemorySegment.ofAddress(address)
        .reinterpret(Long.BYTES)
        .get(ValueLayout.JAVA_LONG_UNALIGNED, 0);
  }
}
