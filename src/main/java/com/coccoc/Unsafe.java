package com.coccoc;

import java.lang.reflect.Field;

@SuppressWarnings("removal")
public class Unsafe {

  public static final sun.misc.Unsafe UNSAFE;

  static {
    try {
      Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UNSAFE = (sun.misc.Unsafe) field.get(null);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }


  public static int getInt(long address) {
    return UNSAFE.getInt(address);
  }

  public static long getLong(long address) {
    return UNSAFE.getLong(address);
  }
}
