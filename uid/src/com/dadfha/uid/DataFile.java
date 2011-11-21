package com.dadfha.uid;

import java.util.Map;
import java.util.TreeMap;

public class DataFile {

	public enum CascadeMode {
		UIDC_NOCSC	( (byte) 0x01 ),
		UIDC_CSC	( (byte) 0x02 );
		
		private byte code;
		
		private CascadeMode(byte code) {
			this.code = code;
		}
		
		public byte getCode() {
			return code;
		}
	}
	
	private final Ucode dbUcode;
	private final UcodeMask dbMask;
	private final CascadeMode cascadeMode;
	private final Map<Ucode, DataEntry> dataEntry = new TreeMap<Ucode, DataEntry>();			

	public DataFile(Ucode dbUcode, UcodeMask dbMask, CascadeMode mode) {
		this.dbUcode = dbUcode;
		this.dbMask = dbMask;
		cascadeMode = mode;
	}
	
	public final DataEntry getDataEntry(Ucode code) {
		return dataEntry.get(code);
	}
	
	public final void addDataEntry(DataEntry entry) {
		dataEntry.put(entry.getUcode(), entry);
	}

	// TODO set, del
	
}
