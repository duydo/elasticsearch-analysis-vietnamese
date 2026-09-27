package com.coccoc;

import org.elasticsearch.test.ESTestCase;

import java.lang.reflect.Field;

/**
 * Unit tests for the {@link NativeMemory} implementations. No native tokenizer library required.
 */
public class NativeMemoryTests extends ESTestCase {

    public void testUnsafeNativeMemory() throws Exception {
        assertReadsNativeMemory(new UnsafeNativeMemory());
    }

    public void testCreateUsesFfmOnJdk22Plus() throws Exception {
        NativeMemory memory = NativeMemory.create();
        if (Runtime.version().feature() >= 22) {
            assertEquals("com.coccoc.FfmNativeMemory", memory.getClass().getName());
        } else {
            assertSame(UnsafeNativeMemory.class, memory.getClass());
        }
        assertReadsNativeMemory(memory);
    }

    private static void assertReadsNativeMemory(NativeMemory memory) throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);
        long address = unsafe.allocateMemory(3 * Long.BYTES);
        try {
            int intValue = randomInt();
            long longValue = randomLong();
            unsafe.putInt(address + 4, intValue);          // unaligned int
            unsafe.putLong(address + Long.BYTES + 1, longValue); // unaligned long
            assertEquals(intValue, memory.getInt(address + 4));
            assertEquals(longValue, memory.getLong(address + Long.BYTES + 1));
        } finally {
            unsafe.freeMemory(address);
        }
    }
}
