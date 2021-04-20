package com.coccoc;

import java.io.*;
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

	private static final int BUFFER_SIZE = 1024 * 1024 * 16;
	private static final String CHARSET = "iso-8859-1";

	public static void saveUnsafeMemory(OutputStream os, long memory, long size) throws IOException {
		for (long i = memory; i < memory + size; i++) {
			os.write(UNSAFE.getByte(i));
		}
	}

	public static long readToUnsafeMemory(File file) throws IOException {
		long len = file.length();
		long memory = UNSAFE.allocateMemory(len);
		try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
			for (long i = 0; i < len; i++) {
				UNSAFE.putByte(memory + i, (byte) fis.read());
			}
		}
		return memory;
	}

	public static float getFloat(byte[] buffer, int offset) {
		return UNSAFE.getFloat(buffer, (long) (sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + offset));
	}

	public static double getDouble(byte[] buffer, int offset) {
		return UNSAFE.getDouble(buffer, (long) (sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + offset));
	}

	public static long getLong(byte[] buffer, int offset) {
		return UNSAFE.getLong(buffer, (long) (sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + offset));
	}

	public static int getInt(byte[] buffer, int offset) {
		return UNSAFE.getInt(buffer, (long) (sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + offset));
	}

	public static short getShort(byte[] buffer, int offset) {
		return UNSAFE.getShort(buffer, (long) (sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + offset));
	}

	public static byte getByte(byte[] buffer, int offset) {
		return UNSAFE.getByte(buffer, (long) (sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + offset));
	}

	public static void writeString(String str, OutputStream os, long unsafeBuffer)
			throws IOException {
		byte[] bytes = str.getBytes(CHARSET);
		writeInt(bytes.length, os, unsafeBuffer);
		os.write(bytes);
	}

	public static void writeInt(int value, OutputStream os, long unsafeBuffer) throws IOException {
		Unsafe.UNSAFE.putInt(unsafeBuffer, value);
		Unsafe.saveUnsafeMemory(os, unsafeBuffer, 4);
	}

	public static String readString(InputStream is) throws IOException {
		int length = readInt(is);
		byte[] bytes = new byte[length];
		is.read(bytes);
		return new String(bytes, CHARSET);
	}

	public static int readInt(InputStream is) throws IOException {
		byte[] bytes = new byte[4];
		is.read(bytes);
		return Unsafe.getInt(bytes, 0);
	}

	public static void copy(byte[] values, int length, long pointer) {
		UNSAFE.copyMemory(values, sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET, null, pointer, length);
	}

	public static void copy(byte[] values, int off, int length, long pointer) {
		UNSAFE.copyMemory(values, sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET + off, null, pointer, length);
	}

	public static void copy(short[] values, int length, long pointer) {
		UNSAFE.copyMemory(values, sun.misc.Unsafe.ARRAY_SHORT_BASE_OFFSET, null, pointer, length * Short.BYTES);
	}

	public static void copy(int[] values, int length, long pointer) {
		UNSAFE.copyMemory(values, sun.misc.Unsafe.ARRAY_INT_BASE_OFFSET, null, pointer, length * Integer.BYTES);
	}

	public static void copy(long[] values, int length, long pointer) {
		UNSAFE.copyMemory(values, sun.misc.Unsafe.ARRAY_LONG_BASE_OFFSET, null, pointer, length * Long.BYTES);
	}
}
