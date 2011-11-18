package com.dadfha.uid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UrpPacket {
	
	/**
	 * Define the structure of UrpPacket field each with specific byte index
	 * @author Wirawit
	 *
	 */
	public enum Field {
		VER				( (short) 0 ),
		SERIAL_NO		( (short) 1 ),
		OPERATOR_LOW	( (short) 2 ), // Can be either Command ID or Error Code depended on the type of packet
		OPERATOR_HIGH	( (short) 3 ),
		RESERVED_LOW	( (short) 4 ),
		RESERVED_HIGH	( (short) 5 ),
		PL_LENGTH_LOW	( (short) 6 ),
		PL_LENGTH_HIGH	( (short) 7 );
		
		private short byteIndex;
		
		private Field(short index) {
			byteIndex = index;
		}
		
		public short getByteIndex() {
			return byteIndex;
		}
	}
	
	private List<Byte> data;
	
	public UrpPacket() {
		
		
		// all array locations, equal to number of byte blocks, are all init to null
		data = new ArrayList<Byte>( Arrays.asList( new Byte[Field.values().length] ) ); 
		
		// The version number is fixed to 1 as of current protocol spec (9/11/2011)
		data.set( Field.VER.getByteIndex(), (byte) 1 );			
		
		// pre-fill the reserved field with 0 as of current protocol spec (9/11/2011)
		setData( Field.RESERVED_LOW.getByteIndex(), (short) 0 );	
		
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
		return getDataShort( Field.OPERATOR_LOW.getByteIndex() );
	}
	
	void setOperator(short operator) {
		setData(Field.OPERATOR_LOW.getByteIndex(), operator);
	}
	
	/**
	 * Total length of ucodeRP packet in 8 octet (byte) blocks
	 * @return short the number of set(s) of 8 octet block(s)
	 */
	public short getLength() {
		return getDataShort( Field.PL_LENGTH_LOW.getByteIndex() );
	}
	
	public String getLengthInHex() {
		return Integer.toHexString( getLength() );	 
	}

	/**
	 * Update the length field to reflect actual size of the packet in Octal-Byte unit (8 bytes).
	 * The method is left with default modifier for its subclass to call 
	 * when need to update its own length of data structure
	 */
	final void updateLength() {
		short plLength = (short)( data.size() / 8 ); // ??? considering change this to shift right 3 times? 
		plLength = (short) ( plLength + getExtLength() );
		setData( Field.PL_LENGTH_LOW.getByteIndex(), plLength );
	}
	
	/**
	 * This method allow subclass to add length of its own data storage for other fields
	 * @return short the added length
	 */
	short getExtLength() {
		return 0;
	}
	
	public List<Byte> getData() {
		return Collections.unmodifiableList(data);
	}	
	
	public byte getData(int index) {
		return data.get(index);
	}
	
	/**
	 * Get 2 byte blocks into 1 short from an index
	 * @param index
	 * @return short the combined bytes of index and index+1 from little endian to big endian order
	 */
	public short getDataShort(int index) {
		return (short) ( ( data.get( index + 1 ) << 8 ) | 
				data.get( index ) );		
	}	
	
	public void setData(int index, byte data) {
		this.data.set( index, data );
	}	
	
	/**
	 * Save short in byte array list from index to index + 1
	 * @param index
	 * @param data
	 */
	public void setData(int index, short data) {		
		this.data.set( index, ( (byte) ( data & 0x00ff ) ) );
		this.data.set( index + 1, ( (byte) ( ( data & 0xff00 ) >>> 8 ) ) );
	}
	
	public void setData(int index, int data) {
		this.data.set( index, ( (byte) ( data & 0x000000ff ) ) );
		this.data.set( index + 1, ( (byte) ( ( data & 0x0000ff00 ) >>> 8 ) ) );
		this.data.set( index + 2, ( (byte) ( ( data & 0x00ff0000 ) >>> 16 ) ) );
		this.data.set( index + 3, ( (byte) ( ( data & 0xff000000 ) >>> 24 ) ) );
	}
	
	public void setData(int index, long data) {
		setData(index, ( (int) (data & 0x00000000ffffffffL) ) );
		setData(index + 4, ( (int) ( (data & 0xffffffff00000000L) >>> 32 ) ) );
	}
	
	public void setData128(int index, long low, long high) {
		setData(index, low);
		setData(index + 8, high);
	}	
	
	
	/**
	 * add new byte to the collection, each adding will result in update of the packet length field
	 * @param data
	 * @return int index of currently add byte
	 */
	public int addByte(byte data) {
		int head = this.data.size();
		this.data.add(data);
		updateLength();
		return head;
	}
	
	/**
	 * add new short to the collection, each adding will result in update of the packet length field
	 * @param data
	 * @return int index of currently add short
	 */
	public int addShort(short data) {
		int head = this.data.size();
		this.data.add( (byte) (data & 0x00ff) );
		this.data.add( (byte) ( (data & 0xff00) >>> 8 ) );
		updateLength();
		return head;
	}
	
	public int addInt(int data) {
		int head = this.data.size();
		this.data.add( (byte) (data & 0x000000ff) );
		this.data.add( (byte) ( (data & 0x0000ff00) >>> 8 ) );
		this.data.add( (byte) ( (data & 0x00ff0000) >>> 16 ) );
		this.data.add( (byte) ( (data & 0xff000000) >>> 24 ) );
		updateLength();
		return head;
	}
	
	public int addLong(long data) {
		int head = addInt( (int) (data & 0x00000000ffffffffL ) );
		addInt( (int) ( (data & 0xffffffff00000000L ) >>> 32 ) );
		return head;
	}
	
	public int add128(long low, long high) {
		int head = addLong(low);
		addLong(high);
		return head;		
	}
	

}
