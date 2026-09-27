package com.coccoc;

import java.lang.reflect.Field;

/**
 * {@link NativeMemory} backed by {@code sun.misc.Unsafe}. Only used on JDK 21.
 */
final class UnsafeNativeMemory implements NativeMemory {

    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public int getInt(long address) {
        return UNSAFE.getInt(address);
    }

    @Override
    public long getLong(long address) {
        return UNSAFE.getLong(address);
    }
}
