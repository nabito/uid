package com.dadfha.uid.server;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.dadfha.uid.Ucode;

/**
 * Data File for ucode store 
 * @author nabito
 *
 */
public class DataFile {

	public enum CascadeMode {
		UIDC_NOCSC	( (byte) 0x01 ),	// NOT providing cascade search function
		UIDC_CSC	( (byte) 0x02 );	// Providing cascade search function for the ucode resolution server
		
		private byte code;
		
		private CascadeMode(byte code) {
			this.code = code;
		}
		
		public byte getCode() {
			return code;
		}
	}
	
	private Ucode dbUcode;
	private Ucode dbMask;
	private CascadeMode cascadeMode;
	private final Map<Ucode, DataEntry> dataEntry = new TreeMap<Ucode, DataEntry>();			

	public DataFile(Ucode dbUcode, Ucode dbMask, CascadeMode cascadeMode) {
		this.dbUcode = dbUcode;
		this.dbMask = dbMask;
		this.cascadeMode = cascadeMode;
	}
	
	public final Ucode getDbUcode() {
		return dbUcode;
	}
	
	public final void setDbUcode(Ucode code) {
		dbUcode = code;
		System.gc();
	}
	
	public final Ucode getDbMask() {
		return dbMask;
	}
	
	public final void setDbMask(Ucode mask) {
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
	
	public final void clearDataEntry() {
		dataEntry.clear();
		System.gc();
	}
	
	/**
	 * Get DataEntry Collection view
	 * @return Collection<DataEntry>
	 */
	public final Collection<DataEntry> getDataEntriesView() {
		return dataEntry.values();
	}
	
	/**
	 * Check whether a ucode is within supported ucode space of data file
	 * @return boolean
	 */
	public final boolean isInSpace(Ucode code) {
		// FIXME check correctness of this code, it doesn't work right?
		return ( dbMask.bitwiseAND( dbUcode ).toString().equals( dbMask.bitwiseAND( code ).toString() ) ) ? true : false;
	}
	
}
