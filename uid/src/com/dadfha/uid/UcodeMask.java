package com.dadfha.uid;

// FIXME consider removing this class and use just Ucode type instead
public class UcodeMask extends Ucode {
	
	public UcodeMask(long highBits, long lowBits) {
		super(highBits, lowBits);
	}
	
	public final Ucode mask(Ucode code) {
		long[] l = code.getBitsArray();
		l[0] = l[0] & highBits;
		l[1] = l[1] & lowBits;
		return new Ucode(l[0], l[1]);
	}

}
