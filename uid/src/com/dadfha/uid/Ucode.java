package com.dadfha.uid;

public class Ucode implements Comparable {
	
	long highBits;
	long lowBits;
	
	public Ucode() {
		this(0, 0);
	}
	
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
	
	public String toString() {
		return String.format("%x", highBits) + String.format("%x", lowBits);
	}

	// TODO this must be completed!
	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

}
