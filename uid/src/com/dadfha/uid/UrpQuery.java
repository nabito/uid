package com.dadfha.uid;

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
	
	private Command commandId;
	
	public UrpQuery() {
		
	}
	
	public Command getCommandId() {
		return commandId;
	}
	
	public void setCommandId(Command command) {
		commandId = command;
	}
	
	
}
