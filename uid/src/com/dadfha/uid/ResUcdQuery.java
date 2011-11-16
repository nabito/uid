package com.dadfha.uid;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dadfha.uid.UcodeRP.UcodeType;

/**
 * Class representing packet structure for the res_ucd query command
 * @author Wirawit
 *
 */
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
		
		public static QueryMode valueOf(short code) {
			return table.get(code);
		}
	}
	
	public enum QueryAttribute {
		UIDC_ATTR_ANONYMOUS	( (short) 0x0000 ),
		UIDC_ATTR_RS		( (short) 0x0001 ),
		UIDC_ATTR_SS		( (short) 0x0002 ),
		UIDC_ATTR_SIGS		( (short) 0x0003 ),
		UID_USER			( (short) 0x00ff );
		
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
	
	public enum ResUcdQueryField {
		T				( (short) 8 ),
		RESERVED		( (short) 12 ),
		QUERY_MODE		( (short) 16 ),
		QUERY_ATTRIBUTE	( (short) 18 ),
		UCODE_TYPE		( (short) 20 ),
		UCODE_LENGTH	( (short) 22 ),
		QUERY_UCODE		( (short) 24 ); // the first byte index of Query Ucode Field
		
		private short byteIndex;
		
		private ResUcdQueryField(short index) {
			byteIndex = index;
		}
		
		public short getByteIndex() {
			return byteIndex;
		}
	}
	
	private List<Long> queryUcode = new ArrayList<Long>();
	private List<Long> queryMask = new ArrayList<Long>();
	
	public ResUcdQuery(int t, QueryMode queryMode, QueryAttribute queryAttribute, short ucodeType, short ucodeLength) {
		// init all fields
		addInt(t);
		addInt(0);		
		addShort(queryMode.getCode());
		addShort(queryAttribute.getCode());
		addShort(ucodeType);
		addShort(ucodeLength);
		
		assert addInt(t) == ResUcdQueryField.T.getByteIndex();
		assert addInt(0) == ResUcdQueryField.RESERVED.getByteIndex();		
		assert addShort(queryMode.getCode()) == ResUcdQueryField.QUERY_MODE.getByteIndex();
		assert addShort(queryAttribute.getCode()) == ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex();
		assert addShort(ucodeType) == ResUcdQueryField.UCODE_TYPE.getByteIndex();
		assert addShort(ucodeLength) == ResUcdQueryField.UCODE_LENGTH.getByteIndex();		
	}
	
	/**
	 * Get the time of command send time
	 * @return int the time in seconds since 0:00AM, Jan. 1,2000 GMT
	 */
	public int getT() {
		return getData(ResUcdQueryField.T.getByteIndex());
	}	
	
	/**
	 * Command send time
	 * @param time the time in seconds since 0:00AM, Jan. 1,2000 GMT
	 */
	public void setT(int time) {
		setData(ResUcdQueryField.T.getByteIndex(), time);
	}
	
	/**
	 * Get search mode of the ucode resolution DB
	 * @return QueryMode
	 */
	public QueryMode getQueryMode() {
		short mode = getDataShort(ResUcdQueryField.QUERY_MODE.getByteIndex());
		return QueryMode.valueOf(mode); 
	}
	
	/**
	 * Set search mode of the ucode resolution DB
	 * @param mode
	 */
	public void setQueryMode(QueryMode mode) {
		setData(ResUcdQueryField.QUERY_MODE.getByteIndex(), mode.getCode());
	}
	
	/**
	 * Get DataAttribute to be retrieved
	 * @return QueryAttribute
	 */
	public QueryAttribute getQueryAttribute() {
		short attribute = getDataShort(ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex());
		return QueryAttribute.valueOf(attribute);
	}
	
	public UcodeType getUcodeType() {
		short type = getData(ResUcdQueryField.UCODE_TYPE.getByteIndex());
		return UcodeType.valueOf(type);		
	}
	
	public void setUcodeType(UcodeType type) {
		setData(ResUcdQueryField.UCODE_TYPE.getByteIndex(), type.getCode());
	}
	
	public short getUcodeLength() {
		return getData(ResUcdQueryField.UCODE_LENGTH.getByteIndex());
	}
	
	/**
	 * Total length of a queryucode/querymask (byte)
	 */
	public void updateUcodeLength() {
		short length = (short) ( (getLength() * 8 ) - ResUcdQueryField.QUERY_UCODE.getByteIndex() );
		assert length >= 0 : length;		
		setData(ResUcdQueryField.UCODE_LENGTH.getByteIndex(), length);
	}
	
	/**
	 * Get a query ucode
	 * @param index added sequence of ucode  
	 * @return long the query ucode
	 */
	public long getQueryUcode(int index) {
		return queryUcode.get(index);
	}
	
	public void setQueryUcode(int index, long data) {
		queryUcode.set(index, data);
	}
	
	/**
	 * Add query ucode and its mask bits
	 * @param ucode
	 * @param mask
	 */
	public void addQuery(long ucode, long mask) {
		queryUcode.add(ucode);
		queryMask.add(mask);
		updateLength();
	}
	
	/**
	 * Get a query mask
	 * @param index added sequence of mask
	 * @return long the query mask
	 */
	public long getQueryMask(int index) {
		return queryMask.get(index);
	}
	
	public void setQueryMask(int index, long data) {
		queryMask.set(index, data);
	}
	
	short getExtLength() {		
		// FIXME if the value exceed Short.MAX_VALUE, 32767, then it won't fit in short
		// because all java type are signed, we need to change to bigger type or define our own unsigned class
		// or to beware about how we should expect the value in everyline of code (sounds best? should sum a note)
		return (short) queryUcode.size();
	}
	
}
