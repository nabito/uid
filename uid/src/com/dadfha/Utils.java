package com.dadfha;

import java.util.Arrays;

public class Utils {

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	public static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}	

	public static <T> T[] concatAll(T[] first, T[]... rest) {
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
	
	// FIXME this is done in little endian! but from now it should be big endian!
	public static Byte[] toByteArray(Long[] longArray) {
		Byte[] byteArray = new Byte[longArray.length * 8];
		int i = 0;
		for(Long l : longArray) {			
			byteArray[i] = (byte) (l & 0xff); 
			byteArray[i+1] = (byte) ( (l & 0xff00) >>> 8 );
			byteArray[i+2] = (byte) ( (l & 0xff0000) >>> 16 );
			byteArray[i+3] = (byte) ( (l & 0xff000000) >>> 24 );
			
			byteArray[i+4] = (byte) ( (l & 0xff00000000L) >>> 32 );
			byteArray[i+5] = (byte) ( (l & 0xff0000000000L) >>> 40 );
			byteArray[i+6] = (byte) ( (l & 0xff000000000000L) >>> 48 );
			byteArray[i+7] = (byte) ( (l & 0xff00000000000000L) >>> 56 );
			
			i = i + 8;
		}
		return byteArray;
	}

}
