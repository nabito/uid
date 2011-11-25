package com.dadfha.uid;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dadfha.Utils;
import com.dadfha.uid.server.DataEntry.DataAttribute;
import com.dadfha.uid.server.DataEntry.DataType;


public final class ResUcdRecieve extends UrpRecieve {
	
	public enum ResolveMode {
		UIDC_RSMODE_RESOLUTION	( (short) 0x0000 ),
		UIDC_RSMODE_CACHE		( (short) 0x0001 ),
		UIDC_RSMODE_CASCADE		( (short) 0x0002 );
		
		private short code;
		private static Map<Short, ResolveMode> table = new HashMap<Short, ResolveMode>();
		
		static {
			for(ResolveMode rm : EnumSet.allOf(ResolveMode.class)) {
				table.put(rm.getCode(), rm);
			}
		}
		
		private ResolveMode(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}		
		
		public static ResolveMode valueOf(short code) {
			return table.get(code);
		}
	}	

	public enum ResUcdRecieveField {
		TTL				( (short) 8 ),
		RESERVED		( (short) 12 ),
		DATA_VERSION	( (short) 14 ),
		RESOLVE_MODE	( (short) 16 ),
		DATA_ATTRIBUTE	( (short) 18 ),
		DATA_TYPE		( (short) 20 ),
		DATA_LENGTH		( (short) 22 );
		
		private short byteIndex;
		
		private ResUcdRecieveField(short index) {
			byteIndex = index;
		}
		
		public short getByteIndex() {
			return byteIndex;
		}
	}
	
	private final List<Long> data = new ArrayList<Long>();
	private final List<Long> returnMask = new ArrayList<Long>();
	
	public ResUcdRecieve(int ttl, short dataVersion, ResolveMode mode, DataAttribute attribute, DataType type, short dataLength) {
		
		int temp;
		
		temp = addInt(ttl);
		assert temp == ResUcdRecieveField.TTL.getByteIndex();
		temp = addShort( (short) 0 );
		assert temp == ResUcdRecieveField.RESERVED.getByteIndex();
		temp = addShort(dataVersion);
		assert temp == ResUcdRecieveField.DATA_VERSION.getByteIndex();
		temp = addShort(mode.getCode());
		assert temp == ResUcdRecieveField.RESOLVE_MODE.getByteIndex();
		temp = addShort(attribute.getCode());
		assert temp == ResUcdRecieveField.DATA_ATTRIBUTE.getByteIndex();		
		temp = addShort(type.getCode());
		assert temp == ResUcdRecieveField.DATA_TYPE.getByteIndex();
		temp = addShort(dataLength);
		assert temp == ResUcdRecieveField.DATA_LENGTH.getByteIndex();
		
	}
	
	/**
	 * Get expiration date of retrieved data (seconds)
	 * @return int date of expiration in seconds
	 */
	public final int getTTL() {
		return getData(ResUcdRecieveField.TTL.getByteIndex());
	}
	
	/**
	 * Specify the expiration date of data (seconds)
	 * @param time in seconds
	 */
	public final void setTTL(int time) {
		setData(ResUcdRecieveField.TTL.getByteIndex(), time);
	}
	
	/**
	 * Get data version of the search
	 * @return short data version
	 */
	public final short getDataVersion() {
		return getData(ResUcdRecieveField.DATA_VERSION.getByteIndex());
	}
	
	/**
	 * Specify data version 
	 * @param dataVersion
	 */
	public final void setDataVersion(short dataVersion) {
		setData(ResUcdRecieveField.DATA_VERSION.getByteIndex(), dataVersion);
	}
	
	/**
	 * Get Resolve Mode of retrieved database
	 * @return ResolveMode
	 */
	public final ResolveMode getResolveMode() {
		short mode = getData(ResUcdRecieveField.RESOLVE_MODE.getByteIndex());
		return ResolveMode.valueOf(mode);
	}
	
	/**
	 * Specify Resolve Mode
	 * @param mode
	 */
	public final void setResolveMode(ResolveMode mode) {
		setData(ResUcdRecieveField.RESOLVE_MODE.getByteIndex(), mode.getCode());
	}
	
	/**
	 * Get Data Attribute
	 * @return DataAttribute
	 */
	public final DataAttribute getDataAttribute() {
		short attribute = getData(ResUcdRecieveField.DATA_ATTRIBUTE.getByteIndex());
		return DataAttribute.valueOf(attribute);		
	}
	
	/**
	 * Specify Data Attribute
	 * @param attribute
	 */
	public final void setDataAttribute(DataAttribute attribute) {
		setData(ResUcdRecieveField.DATA_ATTRIBUTE.getByteIndex(), attribute.getCode());
	}
	
	/**
	 * Get Data Type
	 * @return DataType
	 */
	public final DataType getDataType() {
		short type = getData(ResUcdRecieveField.DATA_TYPE.getByteIndex());
		return DataType.valueOf(type);		
	}
	
	/**
	 * Specify Data Type
	 * @param type
	 */
	public final void setDataType(DataType type) {
		setData(ResUcdRecieveField.DATA_TYPE.getByteIndex(), type.getCode());
	}
	
	/**
	 * Get Data Length in byte
	 * @return short numbers of byte in data section
	 */
	public final short getDataLength() {
		return getData(ResUcdRecieveField.DATA_LENGTH.getByteIndex());
	}
	
	/**
	 * Update the Data Length in byte
	 * @throws RuntimeException when the length data is bigger than field size
	 */
	private final void updateDataLength() {
		int length = ( data.size() * 8 );
		if( length > Math.pow(2, Short.SIZE) ) throw new RuntimeException("The data length value exceed the size of length field");
		short dataLength = (short) length;
		setData( ResUcdRecieveField.DATA_LENGTH.getByteIndex(), dataLength );
	}
	
	/**
	 * Get resolved data
	 * @param index the array index of data
	 * @return long resolved data
	 */
	public final long getResUcdData(int index) {
		return data.get(index);
	}
	
	/**
	 * Update data
	 * @param index the array index of data
	 * @param data
	 */
	public final void setResUcdData(int index, long data) {
		this.data.set(index, data);
	}
	
	/**
	 * Add data to the packet
	 * @param data
	 * @return int index of added data
	 */
	public final int addResUcdData(long data) {
		int index = this.data.size();
		this.data.add(data);
		updateDataLength();
		updateLength();
		return index;
	}	
	
	/**
	 * Get Mask Length in byte
	 * @return short
	 */
	public final short getMaskLength() {
		return (short) ( returnMask.size() * 8 );
	}
	
	/**
	 * Get Return Mask
	 * @param index the array index of mask
	 * @return long the mask
	 */
	public final long getMask(int index) {
		return returnMask.get(index);
	}
	
	/**
	 * Set Return Mask
	 * @param index the array index of mask
	 * @param data the mask
	 */
	public final void setMask(int index, long data) {
		returnMask.set(index, data);
	}
	
	/**
	 * Add Return Mask to packet
	 * @param mask
	 * @return int index of the added mask
	 * @throws RuntimeException when the length data is bigger than field size
	 */
	public final int addMask(long mask) {
		int index = returnMask.size();
		returnMask.add(mask);
		int length = (index + 1) * 8;
		if( length > Math.pow(2, Short.SIZE) ) throw new RuntimeException("The mask length value exceed the size of length field");
		updateLength();
		return index;		
	}
	
	/**
	 * Override getSubLength() to provide size counting of subclass own data storage for data and returnmask fields
	 * @return short the length of data in 8-Octet unit
	 */
	@Override
	short getSubLength() {
		// +1 is from the row of reserved bits and masklength field
		return (short) ( data.size() + 1 + returnMask.size() );
	}
	
	/**
	 * Concatenate Resolved Data, masklength row and Return Mask into Byte array
	 */
	@Override
	Byte[] subPack() {
		Long[] d = data.toArray(new Long[0]);
		Byte[] maskLengthRow = { 0, 0, 0, 0, 0, 0, (byte) ( ( getMaskLength() & 0xff00 ) >>> 8 ), (byte) ( getMaskLength() & 0x00ff ) };
		Long[] rm = returnMask.toArray(new Long[0]);
		Byte[] byteArray = Utils.concatAll( Utils.toByteArray(d), maskLengthRow, Utils.toByteArray(rm) );
		return byteArray;
	}
	
}
