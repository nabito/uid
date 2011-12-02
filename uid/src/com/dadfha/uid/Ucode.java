package com.dadfha.uid;

import java.util.Arrays;

import com.dadfha.uid.UcodeRP.UcodeType;

/**
 * ucode of variable size
 * @author Wirawit
 *
 */
public class Ucode implements Comparable<Ucode> {
	
	private long[] data;
	public final UcodeType type;
	
	/**
	 * Construct Ucode with specified Ucode Type (128, 256, 512, or 1024 bits)
	 * by copying supplied array value
	 * @param code in Big Endian byte order
	 * @param type
	 */
	public Ucode(long[] code, UcodeType type) {
		int arraySize = ( 128 * (int) Math.pow(2.0, ( type.getCode() - 1 ) ) ) / Long.SIZE;
		data = new long[arraySize];
		System.arraycopy(code, 0, data, 0, arraySize);
		this.type = type;
	}
	
	/**
	 * Construct 128-bit ucode
	 * @param highBits
	 * @param lowBits
	 */
	public Ucode(long highBits, long lowBits) {
		long[] l = { highBits, lowBits };
		data = l;
		type = UcodeType.UID_128;
	}
	
	/**
	 * Get a copy of array containing ucode bits data
	 * @return long[]
	 */
	public final long[] getBitsArray() {
		long[] copy = new long[data.length];
		System.arraycopy(data, 0, copy, 0, data.length);
		return copy;
	}
	
	/**
	 * Make copy of bits from supplied parameter to ucode
	 * @param bits
	 */
	public final void setBitsArray(long[] bits) {
		if(bits.length != data.length) throw new RuntimeException("Size of ucode does not match");
		System.arraycopy(bits, 0, data, 0, bits.length);		
	}
	
	/**
	 * Bitwise AND operator for Ucode type
	 * @param code
	 * @return Ucode new Ucode object after bitwise AND operation
	 */
	public final Ucode bitwiseAND(Ucode code) {
		if(code.type != type) throw new RuntimeException("Type of ucode does not match");
		long[] l = code.getBitsArray();
		
		for(int i = 0; i < data.length; i++) {
			l[i] = l[i] & data[i];
		}
		return new Ucode(l, type);
	}	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(long bits : data) sb.append(String.format("%x", bits));
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ucode other = (Ucode) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int compareTo(Ucode ucode) {
		// Comparison between hex using string alphabetical compare works 
		// ONLY IF both string are of same length and case
		return this.toString().toLowerCase().compareTo(ucode.toString().toLowerCase());
	}

}
