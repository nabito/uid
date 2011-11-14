package com.dadfha.uid;

public class UrpReceive extends UrpPacket {
	
	public UrpReceive() {
		
	}
	
	public Error getErrorCode(short code) {
		return Error.valueOf(code);
	}
	
	public void setErrorCode(Error error) {
		this.setOperator( error.getCode() );
	}
	
	public void packParameter() {
		
	}

}
