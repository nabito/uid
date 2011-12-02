package com.dadfha;

import java.util.Arrays;

public class Utils {
	
	/**
	 * Give int value of byte when treat as unsigned 
	 * @param unsigned
	 * @return
	 */
	public static final int ubyteToInt(byte b) {
		return Integer.parseInt(String.format("%x", b), 16);
	}
	
	/**
	 * Array concatenation of generic type object array of two
	 * @param first
	 * @param second
	 * @return <T> T[]
	 */
	public static final <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * Array concatenation of generic type object array of more than two parameters
	 * @param first
	 * @param rest
	 * @return <T> T[]
	 */
	public static final <T> T[] concatAll(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
	
	/**
	 * Convert array of Long to array of Byte in Big Endian byte order
	 * This method uses memory copy technique, which is costly, and is not recommended for use extensively
	 * @param longArray
	 * @return Byte[] containing copy of data from longArray
	 */
	public static final byte[] toByteArray(long[] longArray) {
		byte[] byteArray = new byte[longArray.length * 8];
		int i = 0;
		for(long l : longArray) {			
			byteArray[i] = (byte) ( (l & 0xff00000000000000L) >>> 56 );
			byteArray[i+1] = (byte) ( (l & 0xff000000000000L) >>> 48 );
			byteArray[i+2] = (byte) ( (l & 0xff0000000000L) >>> 40 );
			byteArray[i+3] = (byte) ( (l & 0xff00000000L) >>> 32 );
			byteArray[i+4] = (byte) ( (l & 0xff000000) >>> 24 );
			byteArray[i+5] = (byte) ( (l & 0xff0000) >>> 16 );
			byteArray[i+6] = (byte) ( (l & 0xff00) >>> 8 );
			byteArray[i+7] = (byte) (l & 0xff); 
			i = i + 8;
		}
		return byteArray;
	}

}
