package com.dadfha.uid;

import java.util.HashMap;
import java.util.Map;


public class UrpRecieve extends UrpPacket {
	
	public enum Error {
		E_UIDC_OK		( (short) 0x0000 ),		/** Resolved successfully */
		E_UIDC_NOSPT	( (short) 0xffef ),		/** Unsupported function */
		E_UIDC_PAR		( (short) 0xffdf ),		/** Parameter error */
		E_UIDC_NOEXS	( (short) 0xffcc );		/** Entry nonexistence */
		
		private short code;
		private static final Map<Short, Error> table = new HashMap<Short, Error>();
		
	     static {
	    	 for(Error e : Error.values()) 
	               table.put(e.getCode(), e);
	     }		
		
		private Error(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}		
		
		public static Error valueOf(short code) {
			return table.get(code);
		}
	}	
	
	public UrpRecieve() {
		
	}
	
	public UrpRecieve(byte[] byteArray) {
		super(byteArray);
	}
	
	/**
	 * Get Error Code
	 * @return Error
	 */
	public Error getErrorCode() {
		return Error.valueOf(getOperator());
	}
	
	/**
	 * Update Error Code
	 * @param error
	 */
	public void setErrorCode(Error error) {
		setOperator( error.getCode() );
	}
	

}
