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
	
	@Override
	public String toString() {
		return String.format("%x", highBits) + String.format("%x", lowBits);
	}

	@Override
	public int compareTo(Ucode ucode) {
		// Comparison between hex using string alphabetical compare works 
		// ONLY IF both string are of same length and case
		return this.toString().toLowerCase().compareTo(ucode.toString().toLowerCase());
	}

}
