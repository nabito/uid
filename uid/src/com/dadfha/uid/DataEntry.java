package com.dadfha.uid;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Bytes;

public class DataEntry {
	
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
	
	private Ucode ucode;
	private UcodeMask ucodeMask;
	private short dataVersion;
	private int ttl;	
	/**
	 * Type of the node which the data points
	 */
	private DataAttribute attribute;
	private DataType dataType;
	private final List<Byte> data = new ArrayList<Byte>();
	
	public DataEntry(Ucode ucode, UcodeMask ucodeMask, DataAttribute attribute, short dataVersion, int ttl, DataType dataType) {
		this(ucode, ucodeMask, attribute, dataVersion, ttl, dataType, null);
	}
	
	public DataEntry(Ucode ucode, UcodeMask ucodeMask, DataAttribute attribute, short dataVersion, int ttl, DataType dataType, String stringData) {
		this.ucode = ucode;
		this.ucodeMask = ucodeMask;
		this.attribute = attribute;
		this.dataVersion = dataVersion;
		this.ttl = ttl;
		this.dataType = dataType;
		if(stringData != null) this.setStringData(stringData);
	}
	
	public final Ucode getUcode() {
		return ucode;
	}
	
	public final void setUcode(Ucode code) {
		ucode = code;
		System.gc();
	}
	
	public final UcodeMask getUcodeMask() {
		return ucodeMask;
	}
	
	public final void setUcodeMask(UcodeMask mask) {
		ucodeMask = mask;
		System.gc();
	}
	
	public final short getDataVersion() {
		return dataVersion;
	}
	
	public final void setDataVersion(short version) {
		dataVersion = version;
	}
	
	public final int getTTL() {
		return ttl;
	}
	
	/**
	 * Cache expiration date of data entry (seconds)
	 * @param time
	 */
	public final void setTTL(int time) {
		ttl = time;
	}
	
	public final DataAttribute getDataAttribute() {
		return attribute;
	}
	
	public final void setDataAttribute(DataAttribute attribute) {
		this.attribute = attribute;
		System.gc();
	}
	
	public final DataType getDataType() {
		return dataType;
	}
	
	public final void setDataType(DataType type) {
		dataType = type;
		System.gc();
	}
	
	/**
	 * Get String data based on UTF-8 byte array 
	 * @return String
	 */
	public final String getStringData() {
		byte[] byteArray = Bytes.toArray(data);
		String s = null;
		try {
			s = new String(byteArray, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if(s == null) throw new RuntimeException("Cannot get string data");
		}
		return s;
	}	
	
	/**
	 * Define String data using UTF-8 character set
	 * The program will clear all data off (if any) for simplicity of operation
	 * (No need to return both indexing and byte length for later reference)
	 * @param data String data to set
	 */
	public final void setStringData(String data) {
		if(!data.isEmpty()) clearData();
		byte[] byteData = null;
		try {
			byteData = data.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if(byteData == null) throw new RuntimeException("Cannot add data");	
		}		
		List<Byte> l = Bytes.asList(byteData);
		this.data.addAll(l);
	}		
	
	public final byte getData(int index) {
		return data.get(index);
	}
		
	public final void setData(int index, byte data) {
		this.data.set(index, data);
	}
	
	public final int addData(byte data) {
		int index = this.data.size();
		this.data.add(data);
		return index;
	}
	
	public final void clearData() {
		data.clear();
		System.gc();
	}
	
}
