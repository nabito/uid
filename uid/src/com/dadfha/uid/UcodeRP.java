package com.dadfha.uid;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class UcodeRP {
	
	public enum UcodeType {
		UID_128		( (short) 0x0001 ),
		UID_256		( (short) 0x0002 ),
		UID_512		( (short) 0x0003 ),
		UID_1024	( (short) 0x0004 );
		
		private short code;
		private static Map<Short, UcodeType> table = new HashMap<Short, UcodeType>();
		
		static {
			for(UcodeType t : EnumSet.allOf(UcodeType.class)) {
				table.put(t.getCode(), t);
			}
		}
		
		private UcodeType(short type) {
			this.code = type;
		}
		
		public short getCode() {
			return code;
		}		
		
		public static UcodeType valueOf(short code) {
			return table.get(code);
		}
	}
	
	private static final UcodeRP object = new UcodeRP();;
	
	private UcodeRP() {
	}
	
	public static UcodeRP getUcodeRP() {
		return object;
	}
	
}
