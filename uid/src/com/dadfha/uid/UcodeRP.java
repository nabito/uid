package com.dadfha.uid;

public class UcodeRP {
	
	private static final UcodeRP object = new UcodeRP();;
	
	private UcodeRP() {
	}
	
	public static UcodeRP getUcodeRP() {
		return object;
	}
	
	public void hello() {
		System.out.println("hello!");
	}
	
}
