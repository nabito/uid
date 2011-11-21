package com.dadfha.uid;

public class UcodeMask extends Ucode {
	
	public final Ucode mask(Ucode code) {
		long[] l = code.getBitsArray();
		l[0] = l[0] & highBits;
		l[1] = l[1] & lowBits;
		return new Ucode(l[0], l[1]);
	}

}
