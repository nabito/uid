package com.dadfha.uid;

import java.util.HashMap;
import java.util.Map;

public class UrpQuery extends UrpPacket {
	
	public enum Command { 
		RES_UCD		( (short) 0x0001 );
		
		private short code;
		private static Map<Short, Command> table = new HashMap<Short, Command>();
		
		static {
			for(Command c : Command.values()) {
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
	
	/**
	 * Get Command Id
	 * @return Command
	 */
	public Command getCommandId() {
		return Command.valueOf(getOperator());
	}
	
	/**
	 * Update Command Id
	 * @param command
	 */
	public void setCommandId(Command command) {
		this.setOperator(command.getCode());
	}
	
	
}
