package com.dadfha.uid;

import com.dadfha.uid.UcodeRP.UcodeType;
import com.dadfha.uid.UrpQuery.QueryAttribute;
import com.dadfha.uid.UrpQuery.QueryMode;

public class ResUcdQuery extends UrpQuery {

	/*
	private int t;
	private int reserved;
	private QueryMode queryMode;
	private QueryAttribute queryAttribute;
	private UcodeType ucodeType;
	private short ucodeLength;
	*/
	
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
		addInt(t);
		addInt(0);		
		// TODO assert use in the production code??
	}
	
}
