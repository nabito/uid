package com.dadfha.uid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		
		private Error(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}		
	}
	
	/**
	 * the version number is fixed to 1 as of current protocol spec (9/11/2011)
	 */
	private byte version;
	private byte serialNumber;
	
	/**
	 * the reserved fields are fixed to 0 as of current protocol spec (9/11/2011)
	 */
	private byte reservedHigh;
	private byte reservedLow;
	
	private short plLength;
	
	private List<Byte> data;
	
	public UrpPacket() {
		version = 1;
		reservedHigh = 0;
		reservedLow = 0;
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
	
	public void setData(UrpParameter param) {
		// TODO extract the parameter based on insertion order
		updateLength();
	}
	
	public void updateData(byte data, int index) {
		this.data.set(index, data);
	}
	

}
