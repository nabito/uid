package com.dadfha.uid;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ResUcdQuery extends UrpQuery {

	public enum QueryMode {
		UIDC_RSMODE_RESOLUTION	( (short) 0x0000 ),
		UIDC_RSMODE_CACHE		( (short) 0x0001 ),
		UIDC_RSMODE_CASCADE		( (short) 0x0002 );
		
		private short code;
		private static Map<Short, QueryMode> table = new HashMap<Short, QueryMode>();
		
		static {
			for(QueryMode q : EnumSet.allOf(QueryMode.class)) {
				table.put(q.getCode(), q);
			}
		}
		
		private QueryMode(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}		
		
		public static QueryMode valueOf(short mode) {
			return table.get(mode);
		}
	}
	
	public enum QueryAttribute {
		UIDC_ATTR_ANONYMOUS	( (short) 0x0000 ),
		UIDC_ATTR_RS		( (short) 0x0001 ),
		UIDC_ATTR_SS		( (short) 0x0002 ),
		UIDC_ATTR_SIGS		( (short) 0x0003 ),
		UID_USER			( (short) 0x0004 );
		
		private short code;
		private static Map<Short, QueryAttribute> table = new HashMap<Short, QueryAttribute>();
		
		static {
			for(QueryAttribute qa : EnumSet.allOf(QueryAttribute.class)) {
				table.put(qa.getCode(), qa);
			}
		}
		
		private QueryAttribute(short code) {
			this.code = code;
		}
		
		public short getCode() {
			return code;
		}	
		
		public static QueryAttribute valueOf(short code) {
			return table.get(code);
		}
	}	
	
	public enum ResUcdField {
		T				( (short) 8 ),
		RESERVED		( (short) 12 ),
		QUERY_MODE		( (short) 16 ),
		QUERY_ATTRIBUTE	( (short) 18 ),
		UCODE_TYPE		( (short) 20 ),
		UCODE_LENGTH	( (short) 22 );
		
		private short byteIndex;
		
		private ResUcdField(short index) {
			byteIndex = index;
		}
		
		public short getByteIndex() {
			return byteIndex;
		}
	}
	
	public ResUcdQuery(int t, QueryMode queryMode, QueryAttribute queryAttribute, short ucodeType, short ucodeLength) {
		// init all fields
		addInt(t);
		addInt(0);		
		addShort(queryMode.getCode());
		addShort(queryAttribute.getCode());
		addShort(ucodeType);
		addShort(ucodeLength);
		
		assert addInt(t) == ResUcdField.T.getByteIndex();
		assert addInt(0) == ResUcdField.RESERVED.getByteIndex();		
		assert addShort(queryMode.getCode()) == ResUcdField.QUERY_MODE.getByteIndex();
		assert addShort(queryAttribute.getCode()) == ResUcdField.QUERY_ATTRIBUTE.getByteIndex();
		assert addShort(ucodeType) == ResUcdField.UCODE_TYPE.getByteIndex();
		assert addShort(ucodeLength) == ResUcdField.UCODE_LENGTH.getByteIndex();		
	}
	
	/**
	 * Get the time of command send time
	 * @return int the time in seconds since 0:00AM, Jan. 1,2000 GMT
	 */
	public int getT() {
		return getData(ResUcdField.T.getByteIndex());
	}	
	
	/**
	 * Command send time
	 * @param time the time in seconds since 0:00AM, Jan. 1,2000 GMT
	 */
	public void setT(int time) {
		setData(ResUcdField.T.getByteIndex(), time);
	}
	
	/**
	 * Get search mode of the ucode resolution DB
	 * @return QueryMode
	 */
	public QueryMode getQueryMode() {
		short mode = getDataShort(ResUcdField.QUERY_MODE.getByteIndex());
		return QueryMode.valueOf(mode); 
	}
	
	/**
	 * Set search mode of the ucode resolution DB
	 * @param mode
	 */
	public void setQueryMode(QueryMode mode) {
		setData(ResUcdField.QUERY_MODE.getByteIndex(), mode.getCode());
	}
	
	/**
	 * Get DataAttribute to be retrieved
	 * @return QueryAttribute
	 */
	public QueryAttribute getQueryAttribute() {
		short attribute = getDataShort(ResUcdField.QUERY_ATTRIBUTE.getByteIndex());
		return QueryAttribute.valueOf(attribute);
	}
	
}
