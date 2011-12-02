package com.dadfha.uid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dadfha.Utils;
import com.dadfha.uid.UcodeRP.UcodeType;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

/**
 * Class representing packet structure for the res_ucd query command
 * @author Wirawit
 */
public final class ResUcdQuery extends UrpQuery {

	public enum QueryMode {
		UIDC_RSMODE_RESOLUTION	( (short) 0x0000 ),
		UIDC_RSMODE_CACHE		( (short) 0x0001 ),
		UIDC_RSMODE_CASCADE		( (short) 0x0002 );
		
		private short code;
		private static Map<Short, QueryMode> table = new HashMap<Short, QueryMode>();
		
		static {
			for(QueryMode q : QueryMode.values()) {
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
			for(QueryAttribute qa : QueryAttribute.values()) {
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
		UCODE_LENGTH	( (short) 22 );
		
		private short byteIndex;
		
		private ResUcdQueryField(short index) {
			byteIndex = index;
		}
		
		public short getByteIndex() {
			return byteIndex;
		}
	}
	
	private final List<Long> queryUcode = new ArrayList<Long>();
	private final List<Long> queryMask = new ArrayList<Long>();
	
	/**
	 * Construct ResUcdQuery packet with default values as followed:
	 * 
	 * Property			Value
	 * t				0
	 * querymode		UIDC_RSMODE_RESOLUTION
	 * queryattribute	UIDC_ATTR_RS
	 * ucodetype		UID_128
	 * ucodelength		0
	 * 
	 * meaning the resolution for another ucode resolution server IPv4 address
	 */
	public ResUcdQuery() {
		this(0, QueryMode.UIDC_RSMODE_RESOLUTION, QueryAttribute.UIDC_ATTR_RS, UcodeType.UID_128, (short) 0);
	}
	
	public ResUcdQuery(int t, QueryMode queryMode, QueryAttribute queryAttribute, UcodeType ucodeType, short ucodeLength) {
		int temp;
		
		// Initialize all fields
		temp = addInt(t);
		assert temp == ResUcdQueryField.T.getByteIndex();
		temp = addInt(0);
		assert temp == ResUcdQueryField.RESERVED.getByteIndex();
		temp = addShort(queryMode.getCode());
		assert temp == ResUcdQueryField.QUERY_MODE.getByteIndex();
		temp = addShort(queryAttribute.getCode());
		assert temp == ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex();
		temp = addShort(ucodeType.getCode());
		assert temp == ResUcdQueryField.UCODE_TYPE.getByteIndex();
		temp = addShort(ucodeLength);
		assert temp == ResUcdQueryField.UCODE_LENGTH.getByteIndex();
		
	}
	
	/**
	 * Get the time of command send time
	 * @return int the time in seconds since 0:00AM, Jan. 1,2000 GMT
	 */
	public final int getT() {
		return getData(ResUcdQueryField.T.getByteIndex());
	}	
	
	/**
	 * Command send time
	 * @param time the time in seconds since 0:00AM, Jan. 1,2000 GMT
	 */
	public final void setT(int time) {
		setData(ResUcdQueryField.T.getByteIndex(), time);
	}
	
	/**
	 * Get search mode of the ucode resolution DB
	 * @return QueryMode
	 */
	public final QueryMode getQueryMode() {
		short mode = getDataShort(ResUcdQueryField.QUERY_MODE.getByteIndex());
		return QueryMode.valueOf(mode); 
	}
	
	/**
	 * Set search mode of the ucode resolution DB
	 * @param mode
	 */
	public final void setQueryMode(QueryMode mode) {
		setData(ResUcdQueryField.QUERY_MODE.getByteIndex(), mode.getCode());
	}
	
	/**
	 * Get Data Attribute to be retrieved
	 * @return QueryAttribute
	 */
	public final QueryAttribute getQueryAttribute() {
		short attribute = getDataShort(ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex());
		return QueryAttribute.valueOf(attribute);
	}
	
	/**
	 * Set Data Attribute to be retrieved
	 * @param attribute
	 */
	public final void setQueryAttribute(QueryAttribute attribute) {
		setData(ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex(), attribute.getCode());
	}
	
	/**
	 * Get Ucode type
	 * @return UcodeType
	 * @throws RuntimeException when ucode type value not recognized
	 */
	public final UcodeType getUcodeType() {
		short type = getData(ResUcdQueryField.UCODE_TYPE.getByteIndex());
		UcodeType ut = UcodeType.valueOf(type);
		if(ut == null) throw new RuntimeException("The ucode type field cannot be recognized.");
		return ut;	
	}
	
	/**
	 * Set Ucode type
	 * @param type
	 */
	public final void setUcodeType(UcodeType type) {
		setData(ResUcdQueryField.UCODE_TYPE.getByteIndex(), type.getCode());
	}

	/**
	 * Get Ucode Length
	 * @return short length of queryucode/querymask (byte)
	 */
	public short getUcodeLength() {
		return getData(ResUcdQueryField.UCODE_LENGTH.getByteIndex());
	}
	
	/**
	 * Update total length of a queryucode/querymask (byte)
	 * @throws RuntimeException when the length data is bigger than field size
	 */
	private final void updateUcodeLength() {		
		int length = ( queryUcode.size() * 2 );
		if( length > Math.pow(2, Short.SIZE) ) throw new RuntimeException("The data length value exceed the size of length field");
		short ucodeLength = (short) length;
		setData(ResUcdQueryField.UCODE_LENGTH.getByteIndex(), ucodeLength);
	}
	
	/**
	 * Get the ucode associated with the packet
	 * @return Ucode
	 */
	public final Ucode getQueryUcode() {
		Ucode code = new Ucode(Longs.toArray(queryUcode), getUcodeType()); 
		return code;
	}	
	
	/**
	 * Get query ucode in 128-bit chunk
	 * @param index added sequence of ucode  
	 * @return long[] the query ucode in little endian order
	 * @throws Exception when the index of query ucode is odd number
	 */
	public final long[] getQueryUcode(int index) throws Exception {
		if(index % 2 != 0) throw new Exception("the index value of ucode cannot be odd number");
		long[] l = { queryUcode.get(index), queryUcode.get(index + 1) };
		return l;
	}
	
	/**
	 * Update query ucode in 128-bit chunk
	 * @param index
	 * @param dataLow lower 64-bit data of ucode
	 * @param dataHigh higher 64-bit data of ucode
	 * @throws Exception when the index of query ucode is odd number
	 */
	public final void setQueryUcode(int index, long dataLow, long dataHigh) throws Exception {
		if(index % 2 != 0) throw new Exception("the index value of ucode cannot be odd number");
		queryUcode.set(index, dataHigh);
		queryUcode.set(index + 1, dataLow);
	}
	
	/**
	 * Add query ucode and its mask bits in 128-bit chunk
	 * @param ucodeLow lower 64-bit data of ucode
	 * @param ucodeHigh higher 64-bit data of ucode
	 * @param maskLow lower 64-bit data of mask
	 * @param maskHigh higher 64-bit data of ucode
	 * @return int index of the added ucode for later reference
	 */
	public final int addQuery(long ucodeLow, long ucodeHigh, long maskLow, long maskHigh) {
		int index = queryUcode.size();
		queryUcode.add(ucodeHigh);
		queryUcode.add(ucodeLow);
		queryMask.add(maskHigh);
		queryMask.add(maskLow);
		updateUcodeLength();
		updateLength();		
		return index;
	}
	
	/**
	 * Get the querymask associated with the packet
	 * @return Ucode
	 */
	public final Ucode getQueryMask() {
		Ucode mask = new Ucode(Longs.toArray(queryMask), getUcodeType()); 
		return mask;
	}		
	
	/**
	 * Get query mask in 128-bit chunk
	 * @param index added sequence of ucode NOT of the mask
	 * @return long[] the query mask in little Endian order
	 * @throws Exception 
	 */
	public final long[] getQueryMask(int index) throws Exception {
		if(index % 2 != 0) throw new Exception("the index value of ucode cannot be odd number");
		long[] l = { queryMask.get(index), queryMask.get(index + 1) };
		return l;
	}
	
	/**
	 * Update query mask associated with a ucode in 128-bit chunk
	 * @param index added sequence of ucode NOT of the mask
	 * @param dataLow lower 64-bit data of mask
	 * @param dataHigh higher 64-bit data of mask
	 * @throws Exception when the index of query ucode is odd number
	 */
	public final void setQueryMask(int index, long dataLow, long dataHigh) throws Exception {
		if(index % 2 != 0) throw new Exception("the index value of ucode cannot be odd number");
		index = index + queryUcode.size();
		queryMask.set(index, dataHigh);
		queryMask.set(index + 1, dataLow);
	}
	
	/**
	 * Override getSubLength() to provide size counting of subclass own data storage for queryucode/querymask fields
	 * @return short the length of data in 8-Octet unit
	 */
	@Override
	short getSubLength() {
		return (short) ( queryUcode.size() * 2 );
	}
	
	/**
	 * Concatenate query ucode and query mask data respectively into Byte array
	 */
	@Override
	byte[] subPack() {
		long[] qu = Longs.toArray(queryUcode);
		long[] qm = Longs.toArray(queryMask);
		// ??? Are there any more effective way of converting long[] to byte[] ? 
		// May be directly pack to Network output stream/buffer/channel may produce less overhead
		// if to change, also do it in ResUcdRecieve.subPack()
		byte[] byteArray = Bytes.concat( Utils.toByteArray(qu),  Utils.toByteArray(qm) );
		return byteArray;
	}
	
}
