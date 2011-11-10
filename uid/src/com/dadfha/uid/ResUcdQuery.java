package com.dadfha.uid;

import com.dadfha.uid.UcodeRP.UcodeType;
import com.dadfha.uid.UrpQuery.QueryAttribute;
import com.dadfha.uid.UrpQuery.QueryMode;

public class ResUcdQuery extends UrpQuery {

	private int t;
	private int reserved;
	private QueryMode queryMode;
	private QueryAttribute queryAttribute;
	private UcodeType ucodeType;
	private short ucodeLength;
	
	public ResUcdQuery(int t, QueryMode queryMode, QueryAttribute queryAttribute, short ucodeType, short ucodeLength) {
		
	}
	
}
