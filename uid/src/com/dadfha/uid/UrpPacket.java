package com.dadfha.uid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrpPacket {
	
	/**
	 * Define the structure of UrpPacket field each with specific byte index
	 * @author Wirawit
	 *
	 */
	public enum Field {
		VER			( (short) 0 ),
		SERIAL_NO	( (short) 1 ),
		OPERATOR	( (short) 2 ), // Can be wither Command ID or Error Code depended on the type of packet
		RESERVED	( (short) 4 ),
		PL_LENGTH	( (short) 6 );
		
		private short byteIndex;
		
		private Field(short index) {
			byteIndex = index;
		}
		
		public short getByteIndex() {
			return byteIndex;
		}
	}
	
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
	    	 // XXX for(Error e : Error.values()) // is this better?
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
	
	private List<Byte> data;
	private static final byte BASE_BYTE_BLOCKS = 8;
	// XXX or to get it from Field.values().length but must change field structure to be as per byte for each constant
	// the good: more dynamic, the bad: code will look a bit more verbose
	
	public UrpPacket() {
		
		
		// all array init to null
		data = new ArrayList<Byte>( Arrays.asList( new Byte[BASE_BYTE_BLOCKS] ) ); 
		
		// The version number is fixed to 1 as of current protocol spec (9/11/2011)
		data.set( Field.VER.getByteIndex(), (byte) 1 );			
		
		// pre-fill the reserved field with 0 as of current protocol spec (9/11/2011)
		data.set( Field.RESERVED.getByteIndex(), (byte) 0 );
		data.set( Field.RESERVED.getByteIndex() + 1, (byte) 0 );		
		
	}
	
	public byte getVersion() {
		return data.get( Field.VER.getByteIndex() );
	}
	
	public byte getSerialNumber() {
		return data.get( Field.SERIAL_NO.getByteIndex() );
	}
	
	public void setSerialNumber(byte serialNumber) {
		data.set(Field.SERIAL_NO.getByteIndex(), serialNumber );
	}
	
	// java is big endian, hence the reversal of byte order from spec
	public short getOperator() {
		return (short) ( ( data.get( Field.OPERATOR.getByteIndex() + 1 ) << 8 ) | 
							data.get( Field.OPERATOR.getByteIndex() ) );
	}
	
	void setOperator(short operator) {
		data.set( Field.OPERATOR.getByteIndex(), ( (byte) ( operator & 0x00ff ) ) );
		data.set( Field.OPERATOR.getByteIndex() + 1, ( (byte) ( ( operator & 0xff00 ) >>> 8 ) ) );
	}
	
	public short getLength() {
		return (short) ( ( data.get( Field.PL_LENGTH.getByteIndex() + 1 ) << 8 ) | 
				data.get( Field.PL_LENGTH.getByteIndex() ) );
	}
	
	public String getLengthInHex() {
		return Integer.toHexString( getLength() );	 
	}
	
	private void updateLength() {
		short plLength = (short)( ( data.size() / 8 ) + 1 );
		data.set( Field.PL_LENGTH.getByteIndex(), ( (byte) ( plLength & 0x00ff ) ) );
		data.set( Field.PL_LENGTH.getByteIndex() + 1, ( (byte) ( ( plLength & 0xff00 ) >>> 8 ) ) );		
	}
	
	public List<Byte> getData() {
		return Collections.unmodifiableList(data);
	}	
	
	public void addByte(byte data) {
		this.data.add(data);
	}
	
	public void addShort(short data) {
		this.data.add( (byte) (data & 0x00ff) );
		this.data.add( (byte) ( (data & 0xff00) >>> 8 ) );
		// XXX shall we return this.data.size() to indicate the index of currently add data?
	}
	
	public void updateData(short index, byte data) {
		this.data.set( index, data );
	}	
	
	public void pack() {
		// TODO remove this?
		
		// allow the subclass obj to pack their data
		packParameter();	
		
		// update latest plLength
		updateLength();	//TODO change the value in arraylist directly					
	}
	
	// Rather than declaring as abstract the UrpPacket can also be used solely
	void packParameter() {}
	
	/**
	 * Helper function to save short in byte array list
	 * @param lowIndex
	 * @param highIndex
	 * @param data
	 */
	private void setByteList(int lowIndex, int highIndex, short data) {
		// TODO also change the enum Field to reflect byte addressing
	}
	

}
