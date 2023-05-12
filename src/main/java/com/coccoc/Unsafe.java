package com.coccoc;

import java.lang.reflect.Field;

public class Unsafe {

  public static final sun.misc.Unsafe UNSAFE;

  static {
    sun.misc.Unsafe unsafe = null;
    try {
      Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      unsafe = (sun.misc.Unsafe) field.get(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    UNSAFE = unsafe;
  }


  public static int getInt(long address) {
    return UNSAFE.getInt(address);
  }

  public static long getLong(long address) {
    return UNSAFE.getLong(address);
  }
}
