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
		VER				( (short) 0 ),
		SERIAL_NO		( (short) 1 ),
		OPERATOR_LOW	( (short) 2 ), // Can be wither Command ID or Error Code depended on the type of packet
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
		setByteBlocks( Field.RESERVED_LOW.getByteIndex(), (byte) 0 );	
		
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
		return getByteBlocksInShort( Field.OPERATOR_LOW.getByteIndex() );
	}
	
	void setOperator(short operator) {
		setByteBlocks(Field.OPERATOR_LOW.getByteIndex(), operator);
	}
	
	public short getLength() {
		return getByteBlocksInShort( Field.PL_LENGTH_LOW.getByteIndex() );
	}
	
	public String getLengthInHex() {
		return Integer.toHexString( getLength() );	 
	}
	
	private void updateLength() {
		short plLength = (short)( ( data.size() / 8 ) + 1 );
		setByteBlocks( Field.PL_LENGTH_LOW.getByteIndex(), plLength );
	}
	
	public List<Byte> getData() {
		return Collections.unmodifiableList(data);
	}	
	
	/**
	 * add new byte to the collection, each adding will result in update of the packet length field
	 * @param data
	 * @return int index of currently add byte
	 */
	public int addByte(byte data) {
		this.data.add(data);
		updateLength();
		return this.data.size() - 1;
	}
	
	/**
	 * add new short to the collection, each adding will result in update of the packet length field
	 * @param data
	 * @return int index of currently add short
	 */
	public int addShort(short data) {
		this.data.add( (byte) (data & 0x00ff) );
		this.data.add( (byte) ( (data & 0xff00) >>> 8 ) );
		updateLength();
		return this.data.size() - 2;
	}
	
	public void updateData(int index, byte data) {
		this.data.set( index, data );
	}	
	
	public void pack() {
		// TODO remove this?
		
		// allow the subclass obj to pack their data
		packParameter();				
	}
	
	// Rather than declaring as abstract the UrpPacket can also be used solely
	void packParameter() {}
	
	/**
	 * Get 2 byte blocks into 1 short from an index
	 * @param index
	 * @return short the combined bytes of index and index+1 from little endian to big endian order
	 */
	private short getByteBlocksInShort(int index) {
		return (short) ( ( data.get( index + 1 ) << 8 ) | 
				data.get( index ) );		
	}
	
	/**
	 * Helper function to save short in byte array list from index to index + 1
	 * @param lowIndex
	 * @param data
	 */
	private void setByteBlocks(int index, short data) {
		this.data.set( index, ( (byte) ( data & 0x00ff ) ) );
		this.data.set( index + 1, ( (byte) ( ( data & 0xff00 ) >>> 8 ) ) );		
	}
	

}
