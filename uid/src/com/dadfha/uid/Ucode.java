package com.dadfha.uid;

public class Ucode implements Comparable<Ucode> {
	
	long highBits;
	long lowBits;
		
	public Ucode(long highBits, long lowBits) {
		this.highBits = highBits;
		this.lowBits = lowBits;
	}
	
	public final long getLowBits() {
		return lowBits;
	}
	
	public final void setLowBits(long lowBits) {
		this.lowBits = lowBits;
	}
	
	public final long getHighBits() {
		return highBits;
	}
	
	public final void setHighBits(long highBits) {
		this.highBits = highBits;
	}
	
	public final long[] getBitsArray() {
		long[] l = { highBits, lowBits };
		return l;
	}
	
	public final void setBits(long highBits, long lowBits) {
		this.highBits = highBits;
		this.lowBits = lowBits;
	}
	
	/**
	 * Bitwise AND operator for Ucode type
	 * @param code
	 * @return Ucode
	 */
	public final Ucode bitwiseAND(Ucode code) {
		long[] l = code.getBitsArray();
		l[0] = l[0] & highBits;
		l[1] = l[1] & lowBits;
		return new Ucode(l[0], l[1]);
	}	
	
	@Override
	public String toString() {
		return String.format("%x", highBits) + String.format("%x", lowBits);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (highBits ^ (highBits >>> 32));
		result = prime * result + (int) (lowBits ^ (lowBits >>> 32));
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
		if (highBits != other.highBits)
			return false;
		if (lowBits != other.lowBits)
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
