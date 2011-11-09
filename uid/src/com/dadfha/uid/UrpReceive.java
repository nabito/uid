package com.dadfha.uid;

public class UrpReceive extends UrpPacket {
	
	private short errorCode;
	
	public UrpReceive() {
		
	}
	
	public short getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(UrpPacket.Error error) {
		errorCode = error.getCode();
	}
	
	public void setErrorCode(short code) {
		errorCode = code;
	}

}
