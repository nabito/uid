package com.dadfha.uid;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.dadfha.uid.UrpReceive.Error;

public class UrpQuery extends UrpPacket {
	
	public enum QueryMode {
		UIDC_RSMODE_RESOLUTION	( (short) 0x0000 ),
		UIDC_RSMODE_CACHE		( (short) 0x0001 ),
		UIDC_RSMODE_CASCADE		( (short) 0x0002 );
		
		private short code;
		
		private QueryMode(short code) {
			this.code = code;
		}
		
		public short getMode() {
			return code;
		}		
	}
	
	public enum QueryAttribute {
		UIDC_ATTR_ANONYMOUS	( (short) 0x0000 ),
		UIDC_ATTR_RS		( (short) 0x0001 ),
		UIDC_ATTR_SS		( (short) 0x0002 ),
		UIDC_ATTR_SIGS		( (short) 0x0003 ),
		UID_USER			( (short) 0x0004 );
		
		private short code;
		
		private QueryAttribute(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}	
	}	
	
	public enum Command { 
		RES_UCD		( (short)0x0001 );
		
		private short code;
		private static Map<Short, Command> table = new HashMap<Short, Command>();
		
		static {
			for(Command c : EnumSet.allOf(Command.class)) {
				table.put(c.getCode(), c);
			}
		}
		
		private Command(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}
		
		public static Command valueOf(short code) {
			return table.get(code);
		}
	}	
	
	
	public UrpQuery() {
		
	}
	
	public Command getCommandId(short code) {
		return Command.valueOf(code);
	}
	
	public void setCommandId(Command command) {
		this.setOperator(command.getCode());
	}
	
	
}
