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
	
	private Ucode dbUcode;
	private UcodeMask dbMask;
	private CascadeMode cascadeMode;
	private final Map<Ucode, DataEntry> dataEntry = new TreeMap<Ucode, DataEntry>();			

	public DataFile(Ucode dbUcode, UcodeMask dbMask, CascadeMode mode) {
		this.dbUcode = dbUcode;
		this.dbMask = dbMask;
		cascadeMode = mode;
	}
	
	public final Ucode getDbUcode() {
		return dbUcode;
	}
	
	public final void setDbUcode(Ucode code) {
		dbUcode = code;
		System.gc();
	}
	
	public final UcodeMask getDbMask() {
		return dbMask;
	}
	
	public final void setDbMask(UcodeMask mask) {
		dbMask = mask;
		System.gc();
	}
	
	public final CascadeMode getCascadeMode() {
		return cascadeMode;
	}
	
	public final void setCascadeMode(CascadeMode mode) {
		cascadeMode = mode;
	}
	
	/**
	 * Get a Data Entry from Data File 
	 * @param code
	 * @return
	 */
	public final DataEntry getDataEntry(Ucode code) {
		return dataEntry.get(code);
	}
	
	/**
	 * Replace an old DataEntry object with new one at specified ucode key
	 * @param code
	 * @param entry
	 */
	public final void setDataEntry(Ucode code, DataEntry entry) {
		dataEntry.put(code, entry);
		System.gc();
	}
	
	/**
	 * Add Data Entry into Data File
	 * @param entry
	 * @throws Exception when the added ucode is not in support range of this Data File
	 */
	public final void addDataEntry(DataEntry entry) throws Exception {
		// check if data entry is within supported ucode space
		if( !isInSpace( entry.getUcode() ) ) throw new Exception("The supplied ucode is not in supported range thus cannot be added");
		dataEntry.put(entry.getUcode(), entry);
	}
	
	/**
	 * Remove a Data Entry with the specified ucode
	 * @param code
	 */
	public final void deleteDataEntry(Ucode code) {
		dataEntry.remove(code);
		System.gc();
	}
	
	/**
	 * Check whether a ucode is within supported ucode space of data file
	 * @return boolean
	 */
	public final boolean isInSpace(Ucode code) {
		return ( dbMask.mask( dbUcode ).toString().equals( dbMask.mask( code ).toString() ) ) ? true : false;
	}
	
}
