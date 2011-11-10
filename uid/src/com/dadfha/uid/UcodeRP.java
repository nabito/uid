package com.dadfha.uid;

public class UcodeRP {
	
	public enum UcodeType {
		UID_128		( (short) 0x0001 ),
		UID_256		( (short) 0x0002 ),
		UID_512		( (short) 0x0003 ),
		UID_1024	( (short) 0x0004 );
		
		private short type;
		
		private UcodeType(short type) {
			this.type = type;
		}
		
		public short getType() {
			return type;
		}		
	}
	
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
