package com.dadfha.uid;

import java.util.Map;
import java.util.TreeMap;

import com.dadfha.uid.DataEntry.DataAttribute;
import com.dadfha.uid.DataEntry.DataType;
import com.dadfha.uid.DataFile.CascadeMode;

/**
 * ucode Resolution Server
 * @author nabito
 */
public class UcodeRS {
	
	private final Map<Ucode, DataFile> ucodeSpace = new TreeMap<Ucode, DataFile>();
	private final UcodeRP ucrp = UcodeRP.getUcodeRP();
	
	public UcodeRS() { 
		
		// init server data
		DataFile file1 = new DataFile(new Ucode(0x0efffec000000000L, 0x0000000000040000L), new UcodeMask(0xffffffffffffffffL, 0xffffffffffff0000L), CascadeMode.UIDC_NOCSC);
		try {
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050100L),
												new UcodeMask(0xffffffffffffffffL, 0xffffffffffffff00L),	
												DataAttribute.UIDC_ATTR_SS,
												(short) 1,
												0,
												DataType.UIDC_DATATYPE_UCODE_URL,
												"http://www.uidcenter.org") );
			
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050200L),
					new UcodeMask(0xffffffffffffffffL, 0xffffffffffffff00L),	
					DataAttribute.UIDC_ATTR_RS,
					(short) 1,
					3600,
					DataType.UIDC_DATATYPE_UCODE_IPV4,
					"192.168.10.1") );	
			
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050300L),
					new UcodeMask(0xffffffffffffffffL, 0xffffffffffffff00L),	
					DataAttribute.UIDC_ATTR_RS,
					(short) 2,
					0,
					DataType.UIDC_DATATYPE_UCODE_IPV4,
					"192.168.10.2") );			
			
		} catch (Exception e) {
			// Return message to user that the the supplied ucode is not supported in this space
		}
		DataFile file2 = new DataFile(new Ucode(0x0efffec000000000L, 0x0000000000050000L), new UcodeMask(0xffffffffffffffffL, 0xffffffffffff0000L), CascadeMode.UIDC_CSC);
		ucodeSpace.put(file1.getDbUcode(), file1);
		ucodeSpace.put(file2.getDbUcode(), file2);
		
	}
	
	// TODO first own simple server, second Netty framework, third Cassandra DB
	
	
}
