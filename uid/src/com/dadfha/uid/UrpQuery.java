package com.dadfha.uid;

public class UrpQuery extends UrpPacket {
	
	private short commandId;
	
	public UrpQuery() {
		
	}
	
	public short getCommandId() {
		return commandId;
	}
	
	public void setCommandId(short id) {
		commandId = id;
	}
	
	public void setCommandId(UrpPacket.Command command) {
		this.commandId = command.getCode();
	}
	
	
}
