package com.dadfha.uid;


import com.dadfha.uid.server.UcodeRS;

public class Main {

	// TODO always throw Exception when reverse enum value cannot be matched
	// don't forget to exclude assert in production release: http://download.oracle.com/javase/1.4.2/docs/guide/lang/assert.html#enable-disable

	public static void main(String[] args) {
		
		UcodeRS server = new UcodeRS();		
		server.initServer();
		server.initMockDbData();

	}

}
