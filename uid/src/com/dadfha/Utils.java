package com.dadfha;

import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Utils {
	
	/**
	 * Give int value of byte when treat as unsigned 
	 * @param b
	 * @return
	 */
	public static final int ubyteToInt(byte b) {
		return Integer.parseInt(String.format("%x", b), 16);
	}
	
	/**
	 * Give int value of short when treat as unsigned
	 * @param s
	 * @return
	 */
	public static final int ushortToInt(short s) {
		return Integer.parseInt(String.format("%x", s), 16);
	}	
	
	/**
	 * Give long value of int when treat as unsigned
	 * @param i
	 * @return
	 */
	public static final long uintToLong(int i) {
		return Long.parseLong(String.format("%x", i), 16);
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
	
	/**
	 * Adding byte array to list of long in Big Endian order.
	 * The list from parameter get modified directly.
	 * @param byteArray
	 * @param list
	 */
	public static final void addBytesToLongList(byte[] byteArray, List<Long> list) {
		ByteBuffer bb = ByteBuffer.wrap(byteArray);
		bb.clear();
		
		try {
			while(bb.hasRemaining())
				list.add(bb.getLong());
		} catch (BufferUnderflowException e) {
			// Check the padding needs
			byte offset = (byte) (byteArray.length % 8);
			int length = byteArray.length;
			long lastLong = 0;
			
			// Convert byte array to long with 0 padding left
			for(int i = length - offset; i < length; i++) {
				// Since the shift is beyond int value cast to long is needed
				lastLong |= ( (long) byteArray[i] ) << ( 8 - ( i - (length - offset) + 1 ) ) * 8; 				
			}
			// Add the 0 padded long
			list.add(lastLong);
		}		
	}	
	
	/**
	 * Convert ByteBuffer to byte array, auto copy buffer contents if not already backed by byte array
	 * @param bb
	 * @return byte[]
	 */
	public static final byte[] byteBufferToByteArray(ByteBuffer bb) {		
		// Get buffer ready for read by reset the readIndex
		bb.clear();
		byte[] buffer = null;
		
		if (bb.hasArray()) {
			buffer = bb.array(); // this method only applicable when ByteBuffer is backed by byte[]
		} else {
			buffer = new byte[bb.capacity()];
			bb.get(buffer, 0, buffer.length);
		}		
		return buffer;		
	}
	
	/**
	 * Decode UTF-8 encoded String from byte array
	 * @param byteArray
	 * @return String
	 */
	public static final String bytesToUTF8String(byte[] byteArray) {
		String s = null;
		try {
			s = new String(byteArray, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if(s == null) throw new RuntimeException("Cannot get string data");
		}
		return s;		
	}
	
	/**
	 * Get time in seconds from Y2K Midnight 1-1-2000 00:00 GMT
	 * @return
	 */
	public static final int getSecondsFromY2K() {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+0000"));
		c.clear();
		c.set(2000, 1, 1, 0, 0, 0);
		// c.complete();		
		return (int) ( ( Calendar.getInstance(TimeZone.getTimeZone("GMT+0000")).getTimeInMillis() - c.getTimeInMillis() ) * 1000 );		
	}

}
