package com.dadfha.uid;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dadfha.Utils;


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
	
	public enum DataAttribute {
		UIDC_ATTR_RS		( (short) 0x0001 ),
		UIDC_ATTR_SS		( (short) 0x0002 ),
		UIDC_ATTR_SIGS		( (short) 0x0003 ),
		UID_USER			( (short) 0x00ff );
		
		private short code;
		private static Map<Short, DataAttribute> table = new HashMap<Short, DataAttribute>();
		
		static {
			for(DataAttribute da : EnumSet.allOf(DataAttribute.class)) {
				table.put(da.getCode(), da);
			}
		}
		
		private DataAttribute(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}	
		
		public static DataAttribute valueOf(short code) {
			return table.get(code);
		}
	}	
	
	public enum DataType {
		UIDC_DATATYPE_UCODE_128		( (short) 0x0001 ),
		UIDC_DATATYPE_UCODE_256		( (short) 0x0002 ),
		UIDC_DATATYPE_UCODE_384		( (short) 0x0003 ),
		UIDC_DATATYPE_UCODE_512		( (short) 0x0004 ),
		UIDC_DATATYPE_UCODE_IPV4	( (short) 0x0011 ),
		UIDC_DATATYPE_UCODE_IPV6	( (short) 0x0012 ),
		UIDC_DATATYPE_UCODE_URL		( (short) 0x0013 ),
		UIDC_DATATYPE_UCODE_HOST	( (short) 0x0014 ),
		UIDC_DATATYPE_UCODE_EMAIL	( (short) 0x0021 ),
		UIDC_DATATYPE_UCODE_PHONE	( (short) 0x0031 ),
		UIDC_DATATYPE_UCODE_TXT		( (short) 0x00fe ),
		UIDC_DATATYPE_UCODE_USER	( (short) 0x00ff );
		
		private short code;
		private static Map<Short, DataType> table = new HashMap<Short, DataType>();
		
		static {
			for(DataType dt : EnumSet.allOf(DataType.class)) {
				table.put(dt.getCode(), dt);
			}
		}		
		
		private DataType(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}

		public static DataType valueOf(short code) {
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
	
	private List<Long> data = new ArrayList<Long>(); 
	private List<Long> returnMask = new ArrayList<Long>();
	
	public ResUcdRecieve(int ttl, short dataVersion, ResolveMode mode, DataAttribute attribute, DataType type, short dataLength) {
		addInt(ttl);
		addShort( (short) 0 );
		addShort(dataVersion);
		addShort(mode.getCode());
		addShort(attribute.getCode());
		addShort(type.getCode());
		addShort(dataLength);
		
		assert addInt(ttl) == ResUcdRecieveField.TTL.getByteIndex();
		assert addShort( (short) 0 ) == ResUcdRecieveField.RESERVED.getByteIndex();
		assert addShort(dataVersion) == ResUcdRecieveField.DATA_VERSION.getByteIndex();
		assert addShort(mode.getCode()) == ResUcdRecieveField.RESOLVE_MODE.getByteIndex();
		assert addShort(attribute.getCode()) == ResUcdRecieveField.DATA_ATTRIBUTE.getByteIndex();
		assert addShort(type.getCode()) == ResUcdRecieveField.DATA_TYPE.getByteIndex();
		assert addShort(dataLength) == ResUcdRecieveField.DATA_LENGTH.getByteIndex();
	}
	
	
	public int getTTL() {
		return getData(ResUcdRecieveField.TTL.getByteIndex());
	}
	
	public void setTTL(int time) {
		setData(ResUcdRecieveField.TTL.getByteIndex(), time);
	}
	
	public short getDataVersion() {
		return getData(ResUcdRecieveField.DATA_VERSION.getByteIndex());
	}
	
	public void setDataVersion(short dataVersion) {
		setData(ResUcdRecieveField.DATA_VERSION.getByteIndex(), dataVersion);
	}
	
	public ResolveMode getResolveMode() {
		short mode = getData(ResUcdRecieveField.RESOLVE_MODE.getByteIndex());
		return ResolveMode.valueOf(mode);
	}
	
	public void setResolveMode(ResolveMode mode) {
		setData(ResUcdRecieveField.RESOLVE_MODE.getByteIndex(), mode.getCode());
	}
	
	public DataAttribute getDataAttribute() {
		short attribute = getData(ResUcdRecieveField.DATA_ATTRIBUTE.getByteIndex());
		return DataAttribute.valueOf(attribute);		
	}
	
	public void setDataAttribute(DataAttribute attribute) {
		setData(ResUcdRecieveField.DATA_ATTRIBUTE.getByteIndex(), attribute.getCode());
	}
	
	public DataType getDataType() {
		short type = getData(ResUcdRecieveField.DATA_TYPE.getByteIndex());
		return DataType.valueOf(type);		
	}
	
	public void setDataType(DataType type) {
		setData(ResUcdRecieveField.DATA_TYPE.getByteIndex(), type.getCode());
	}
	
	public short getDataLength() {
		// ??? confirm whether "bits" value > Short.MAX but < 255 can be correctly stored after conversion
		// FIXME the order is in Big Endian!!!! Must be Little! Need check for every method!
		return (short) ( data.size() * 8 );
	}
	
	public long getResUcdData(int index) {
		return data.get(index);
	}
	
	public void setResUcdData(int index, long data) {
		this.data.set(index, data);
	}
	
	public int addResUcdData(long data) {
		int index = this.data.size();
		this.data.add(data);
		updateLength();
		return index;
	}	
	
	public short getMaskLength() {
		return (short) ( returnMask.size() * 8 );
	}
	
	public long getMask(int index) {
		return returnMask.get(index);
	}
	
	public void setMask(int index, long data) {
		returnMask.set(index, data);
	}
	
	public int addMask(long mask) {
		int index = returnMask.size();
		returnMask.add(mask);
		updateLength();
		return index;		
	}
	
	short getExtLength() {
		// +1 is from the row of reserved bits and masklength field
		return (short) ( data.size() + 1 + returnMask.size() );
	}
	
	Byte[] subPack() {
		Long[] d = data.toArray(new Long[0]);
		Byte[] maskLengthRow = { 0, 0, 0, 0, 0, 0, (byte) ( ( getMaskLength() & 0xff00 ) >>> 8 ), (byte) ( getMaskLength() & 0x00ff ) };
		Long[] rm = returnMask.toArray(new Long[0]);
		// ??? Are there any more effective way of converting Long[] to Byte[] ?
		Byte[] byteArray = Utils.concat( Utils.toByteArray(d), Utils.toByteArray(rm) );
		return byteArray;
	}
	
}
