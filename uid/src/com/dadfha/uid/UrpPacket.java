package com.dadfha.uid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.dadfha.Utils;
import com.google.common.primitives.Bytes;

public class UrpPacket {
	
	/**
	 * Define the structure of UrpPacket field each with specific byte index (Independent from byte order of data)
	 * @author Wirawit
	 */
	public enum Field {
		VER				( (short) 0 ),
		SERIAL_NO		( (short) 1 ),
		OPERATOR_HIGH	( (short) 2 ), /** Can be either Command ID or Error Code depended on the type of packet */
		OPERATOR_LOW	( (short) 3 ), /** Can be either Command ID or Error Code depended on the type of packet */
		RESERVED_HIGH	( (short) 4 ),
		RESERVED_LOW	( (short) 5 ),
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
	
	/**
	 * The byte array list use to hold value of all fields
	 */
	private final List<Byte> data; // ??? Considering using java.nio.ByteBuffer in the future for better performance
	
	public UrpPacket() {
		
		// All array locations, equal to number of byte blocks, are all init to null
		data = new ArrayList<Byte>( Arrays.asList( new Byte[Field.values().length] ) ); 
		initFieldsData();
	}
	
	/**
	 * Construct packet from byte array
	 */
	public UrpPacket(byte[] byteArray) {
		data = new ArrayList<Byte>( Bytes.asList( Arrays.copyOfRange( byteArray, 0, Field.values().length - 1 ) ) );
		initFieldsData();	
	}
	
	private void initFieldsData() {
		// The version number is fixed to 1 as of current protocol spec (9/11/2011)
		data.set( Field.VER.getByteIndex(), (byte) 1 );			
		
		// Pre-fill the reserved field with 0 as of current protocol spec (9/11/2011)
		setData( Field.RESERVED_HIGH.getByteIndex(), (short) 0 );			
	}
	
	/**
	 * Get Version field
	 * @return int of version
	 */
	public final int getVersion() {
		return Utils.ubyteToInt( data.get( Field.VER.getByteIndex() ) );
	}
	
	/**
	 * Update Version field
	 * @param version
	 */
	public final void setVersion(byte version) {
		data.set( Field.VER.getByteIndex(), version );
	}
	
	/**
	 * Get Serial Number field
	 * @return int of Serial Number
	 */
	public final int getSerialNumber() {
		return Utils.ubyteToInt( data.get( Field.SERIAL_NO.getByteIndex() ) );
	}
	
	/**
	 * Update Serial Number field
	 * @param serialNumber
	 */
	public final void setSerialNumber(byte serialNumber) {
		data.set( Field.SERIAL_NO.getByteIndex(), serialNumber );
	}
	
	/**
	 * Get Operator field
	 * @return int Operator code
	 */
	final int getOperator() {
		return Utils.ushortToInt( getDataShort( Field.OPERATOR_HIGH.getByteIndex() ) );
	}
	
	/**
	 * Update Operator field
	 * @param operator
	 */
	final void setOperator(short operator) {
		setData(Field.OPERATOR_HIGH.getByteIndex(), operator);
	}
	
	/**
	 * Total length of ucodeRP packet in 8 octet (byte) blocks
	 * @return int the number of set(s) of 8 octet block(s)
	 */
	public final int getLength() {
		return Utils.ushortToInt( getDataShort( Field.PL_LENGTH_LOW.getByteIndex() ) );
	}
	
	/**
	 * Get Length field in Hexadecimal representation 
	 * @return String of Hex
	 */
	public final String getLengthInHex() {
		return String.format("%x", getLength());	 
	}

	/**
	 * Update the Length field to reflect actual size of the packet in Octal-Byte unit (8 bytes).
	 * The method is left with default modifier for its subclass to call 
	 * when need to update its own length of data structure
	 * @throws RuntimeException when the length data is bigger than field size
	 */
	final void updateLength() {
		int length = ( data.size() / 8 ) + getSubLength();
		if( length > Math.pow(2, Short.SIZE) ) throw new RuntimeException("The data length value exceed the size of length field");
		short plLength = (short) length;
		setData( Field.PL_LENGTH_LOW.getByteIndex(), plLength );
	}
	
	/**
	 * This method allow subclass to add length of its own data storage for other fields
	 * The length must be in Octal-Byte unit (8 bytes).
	 * @return int the added length
	 */
	int getSubLength() {		
		return 0;
	}
	
	/**
	 * Get read-only copy of packet data collection in byte array list
	 * @return
	 */
	final List<Byte> getData() {
		return Collections.unmodifiableList(data);
	}	
	
	/**
	 * Get data from byte array list
	 * @param index the byte index
	 * @return byte data
	 */
	final byte getData(int index) {
		return data.get(index);
	}
	
	/**
	 * Get 2 byte blocks into 1 short from an index
	 * @param index the array index of data's first byte
	 * @return short the combined bytes of index and index+1 in Big Endian order
	 */
	final short getDataShort(int index) {
		return (short) ( ( data.get( index ) << 8 ) | 
				data.get( index + 1 ) );		
	}	
	
	/**
	 * Get 4 byte blocks into 1 int from an index
	 * @param index the array index of data's first byte
	 * @return int the combined 4 bytes from index in Big Endian order 
	 */
	final int getDataInt(int index) {
		return (int) ( (data.get(index) << 24) | (data.get(index + 1) << 16)
				| (data.get(index + 2) << 8) | data.get(index + 3));
	}
	
	/**
	 * Update data in byte array list
	 * @param index the byte index
	 * @param data byte
	 */
	final void setData(int index, byte data) {
		this.data.set( index, data );
	}	
	
	/**
	 * Update short in byte array list from index to index + 1
	 * @param index the array index of data's first byte
	 * @param data short data
	 */
	final void setData(int index, short data) {				
		this.data.set( index, ( (byte) ( ( data & 0xff00 ) >>> 8 ) ) );
		this.data.set( index + 1, ( (byte) ( data & 0x00ff ) ) );
	}
	
	/**
	 * Update int data in Big Endian order start from index
	 * @param index the array index of data's first byte
	 * @param data int data
	 */
	final void setData(int index, int data) {
		this.data.set( index, ( (byte) ( ( data & 0xff000000 ) >>> 24 ) ) );		
		this.data.set( index + 1, ( (byte) ( ( data & 0x00ff0000 ) >>> 16 ) ) );
		this.data.set( index + 2, ( (byte) ( ( data & 0x0000ff00 ) >>> 8 ) ) );
		this.data.set( index + 3, ( (byte) ( data & 0x000000ff ) ) );
	}
	
	/**
	 * Update long data in Big Endian order start from index
	 * @param index the array index of data's first byte
	 * @param data long data
	 */
	final void setData(int index, long data) {
		setData(index, ( (int) ( (data & 0xffffffff00000000L) >>> 32 ) ) );
		setData(index + 4, ( (int) (data & 0x00000000ffffffffL) ) );		
	}
	
	/**
	 * Update 128-bit data in Big Endian order start from index
	 * @param index the array index of data's first byte
	 * @param low lower 64-bit of data
	 * @param high higher 64-bit of data
	 */
	final void setData128(int index, long low, long high) {
		setData(index, high);
		setData(index + 8, low);
	}	
	
	/**
	 * Add new byte to the collection, each adding will result in update of the packet length field
	 * @param data
	 * @return int index of currently add byte
	 */
	final int addByte(byte data) {
		int head = this.data.size();
		this.data.add(data);
		updateLength();
		return head;
	}
	
	/**
	 * Add copy of the whole byte array to the collection.
	 * @param byteArray
	 * @return int index of first byte added	 
	 */
	final int addBytes(byte[] byteArray, int indexFrom, int indexTo) {
		int head = this.data.size();
		data.addAll( Bytes.asList( Arrays.copyOfRange( byteArray, indexFrom, indexTo ) ) );		
		updateLength();
		return head;
	}
	
	/**
	 * Add new short to the collection, each adding will result in update of the packet length field
	 * @param data
	 * @return int index of currently add short
	 */
	final int addShort(short data) {
		int head = this.data.size();
		this.data.add( (byte) ( (data & 0xff00) >>> 8 ) );
		this.data.add( (byte) (data & 0x00ff) );
		updateLength();
		return head;
	}
	
	/**
	 * Add integer into packet in Big Endian order
	 * @param data
	 * @return int the array index of added data
	 */
	final int addInt(int data) {
		int head = this.data.size();
		this.data.add( (byte) ( (data & 0xff000000) >>> 24) );
		this.data.add( (byte) ( (data & 0x00ff0000) >>> 16 ) );
		this.data.add( (byte) ( (data & 0x0000ff00) >>> 8 ) );
		this.data.add( (byte) (data & 0x000000ff) );
		updateLength();
		return head;
	}
	
	/**
	 * Add long into packet in Big Endian order
	 * @param data
	 * @return int the array index of added data
	 */
	final int addLong(long data) {
		int head = addInt( (int) ( (data & 0xffffffff00000000L ) >>> 32 ) );
		addInt( (int) (data & 0x00000000ffffffffL ) );		
		return head;
	}
	
	/**
	 * Add 128-bit of data into packet in Big Endian order
	 * @param low lower 64-bit of data
	 * @param high higher 64-bit of data
	 * @return int the array index of added data
	 */
	final int add128(long low, long high) {
		int head = addLong(high);
		addLong(low);
		return head;		
	}
	
	/**
	 * Pack data in byte array by extract byte array from data list 
	 * and merge with Byte array from subclass (if any)
	 * @return byte[]
	 */
	public final byte[] pack() {	
		byte[] byteArray = Bytes.concat(Bytes.toArray(data), subPack());
		return byteArray;
	}
	
	/**
	 * Method for subclass to override and provide all of its fields data in order based on spec
	 * @return byte[]
	 */
	byte[] subPack() {
		return null;
	}

}
