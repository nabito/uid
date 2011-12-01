package com.dadfha.uid.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.dadfha.uid.Ucode;

/**
 * ucode Resolution Database
 * @author nabito
 *
 */
public class UcodeRD {
	
	private final Map<Ucode, DataFile> ucodeSpace = new TreeMap<Ucode, DataFile>(); 
	
	public UcodeRD() {		
		
	}
	
	/**
	 * Get Data File based on DB ucode
	 * @param dbUcode
	 * @return DataFile
	 */
	public final DataFile getDataFile(Ucode dbUcode) {		
		return ucodeSpace.get(dbUcode);
	}
	
	/**
	 * Get DataType Collection view
	 * @return Collection<DataFile>
	 */
	public final Collection<DataFile> getDataFilesView() {
		return ucodeSpace.values();
	}
	
	/**
	 * Update Data File based on DB ucode
	 * @param dbUcode
	 * @param file
	 */
	public final void setDataFile(Ucode dbUcode, DataFile file) {
		ucodeSpace.put(dbUcode, file);
		System.gc();
	}
	
	public final void addDataFile(DataFile file) {
		ucodeSpace.put(file.getDbUcode(), file);
	}
	
	public final void deleteDataFile(Ucode dbUcode) {
		ucodeSpace.remove(dbUcode);
		System.gc();
	}
	
	public final void clearDataFile() {
		ucodeSpace.clear();
		System.gc();
	}
	
	/**
	 * Check if a ucode is in range of any managed Data File(s) 
	 * @param ucode
	 * @return Ucode The DB ucode of first Data File found covering the supplied ucode or null if not found 
	 */
	public final Ucode isInSpace(Ucode ucode) {
		Ucode dbUcode = null;
		DataFile df = null;
		Iterator<DataFile> i = ucodeSpace.values().iterator();
		while( i.hasNext() ) {
			df = i.next();
			if( df.isInSpace(ucode) ) {
				dbUcode = df.getDbUcode(); 
				break;		
			}
		}
		return dbUcode;
	}

}
