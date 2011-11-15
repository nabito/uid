package com.dadfha.uid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrpPacket {
	
	public enum Command { 
		RES_UCD		( (short)0x0001 );
		
		private short code;
		
		private Command(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}
	}
	
	public enum Error {
		E_UIDC_OK		( (short) 0x0000 ),
		E_UIDC_NOSPT	( (short) 0xffef ),
		E_UIDC_PAR		( (short) 0xffdf ),
		E_UIDC_NOEXS	( (short) 0xffcc );
		
		private short code;
		private static final Map<Short, Error> table = new HashMap<Short, Error>();
		
	     static {
	    	 //for(Error e : Error.values()) // is this better?
	         for(Error e : EnumSet.allOf(Error.class))
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
	
	/**
	 * The version number is fixed to 1 as of current protocol spec (9/11/2011)
	 */
	private byte version;
	private byte serialNumber;
	
	/**
	 * Can be wither Command ID or Error Code depended on the type of packet
	 */
	short operator;
	
	/**
	 * The reserved fields are fixed to 0 as of current protocol spec (9/11/2011)
	 */
	private short reserved;
	
	private short plLength;
	
	private List<Byte> data;
	
	public UrpPacket() {
		version = 1;
		data = new ArrayList<Byte>();
	}
	
	public byte getVersion() {
		return version;
	}
	
	public byte getSerialNumber() {
		return serialNumber;
	}
	
	public void setSerialNumber(byte serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	public short getOperator() {
		return operator;
	}
	
	void setOperator(short operator) {
		this.operator = operator;
	}
	
	public short getLength() {
		return plLength;
	}
	
	public String getLengthInHex() {
		return Integer.toHexString(plLength);	 
	}
	
	private void updateLength() {
		plLength = (short)( ( data.size() / 8 ) + 1 );
	}
	
	public List<Byte> getData() {
		return Collections.unmodifiableList(data);
	}	
	
	public void setData(byte data) {
		this.data.add(data);
		updateLength();
	}
	
	public void pack() {
		
		// FIXME an alternative approach to all this is to make it simple
		// let's have class member to represent all fields
		// no var to hold everything in order of the packet
		// why? coz' we will hand over the data to Transport Layer protocol buffer in the correct order later
		// so the order of insertion is strictly the sequence of the code!
		
		data.add(version);
		data.add(serialNumber);
		data.add( (byte) ( operator & 0x00ff ) ); // java is big endian
		data.add( (byte) ( (operator & 0xff00 ) >> 8 ) ); 
		data.add( (byte) ( reserved & 0x00ff ) );
		data.add( (byte) ( (reserved & 0xff00 ) >> 8 ) ); 
		data.add( (byte) ( plLength & 0x00ff ) );
		data.add( (byte) ( (plLength & 0xff00 ) >> 8 ) ); 
		
		// allow the subclass obj to pack their data
		packParameter();	
		
		// update latest plLength
		updateLength();	//TODO change the value in arraylist directly					
	}
	
	// Rather than declaring as abstract the UrpPacket can also be used solely
	void packParameter() {}
	
	public void updateData(byte data, int index) {
		this.data.set(index, data);
	}
	

}
