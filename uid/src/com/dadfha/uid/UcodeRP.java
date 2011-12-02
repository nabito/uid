package com.dadfha.uid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.dadfha.uid.ResUcdQuery.QueryAttribute;
import com.dadfha.uid.ResUcdQuery.QueryMode;
import com.dadfha.uid.ResUcdQuery.ResUcdQueryField;
import com.dadfha.uid.UrpQuery.Command;
import com.dadfha.uid.server.DataEntry;
import com.dadfha.uid.server.DataEntry.DataAttribute;
import com.dadfha.uid.server.DataFile;
import com.dadfha.uid.server.UcodeRD;

/**
 * Note: This has potential to become generic packet construction classes
 * @author Wirawit
 *
 */
public class UcodeRP {
	
	public enum UcodeType {
		UID_128		( (short) 0x0001 ),
		UID_256		( (short) 0x0002 ),
		UID_512		( (short) 0x0003 ),
		UID_1024	( (short) 0x0004 );
		
		private short code;
		private static Map<Short, UcodeType> table = new HashMap<Short, UcodeType>();
		
		static {
			for(UcodeType t : UcodeType.values()) {
				table.put(t.getCode(), t);
			}
		}
		
		private UcodeType(short type) {
			this.code = type;
		}
		
		public short getCode() {
			return code;
		}		
		
		public static UcodeType valueOf(short code) {
			return table.get(code);
		}
	}
	
	private static final UcodeRP object = new UcodeRP();	
	private UcodeRD database;
	
	private UcodeRP() {
	}
	
	/**
	 * Get UcodeRP singleton object
	 * @return
	 */
	public static UcodeRP getUcodeRP() {
		return object;
	}
	
	/**
	 * Set ucode Resolution Database
	 * @param database
	 */
	public void setDatabase(UcodeRD database) {
		this.database = database;
	}
	
	/**
	 * Get ucode Resolution Database
	 * @return
	 */
	public UcodeRD getDatabase() {
		return database;
	}
	
	/**
	 * Check if query if of RES_UCD command then call an appropriate method and return requested data (if any)
	 * @param packet
	 * @return Object requested data (if any) or null for nothing
	 * 
	 * Command			Return type
	 * RES_UCD			ResUcdRecieve
	 */
	public final Object processQuery(UrpQuery packet) {
		
		switch( packet.getCommandId() ) {
			case RES_UCD:
				ResUcdQuery ruqPacket = (ResUcdQuery) packet;
				
				Iterator<Ucode> i = ruqPacket.getUcodeList().iterator();
				Iterator<UcodeMask> j = ruqPacket.getUcodeMaskList().iterator();
				DataEntry entry = null;
				// TODO construct response packet
				//ResUcdRecieve rurPacket = new ResUcdRecieve(ttl, dataversion, resolveMode, ... ); 
				while(i.hasNext() && j.hasNext()) {
					entry = resolveUcode(i.next(), j.next(), ruqPacket.getQueryAttribute());
					
				}
				
				// TODO get list of ucode (and mask?) iterate through it, resolve info
				// return ResUcdRecieve obj with resolve data as a result
				
				break;
			default: break;
		}
		
		return null;
	}
	
	public final DataEntry resolveUcode(Ucode code, UcodeMask mask, QueryAttribute attribute) {
		// Check if database has properly initialized
		if(database == null) throw new NullPointerException("Database of type UcodeRD must not be null.");
		
		Iterator<DataFile> i = database.getDataFilesView().iterator();
		DataFile file = null;
		boolean isSpaceMatched = false;
		
		// Search for matching criteria of a data file (Specification page 12)
		// (queryucode & querymask & dbmask) equals (dbucode & querymask & dbmask)
		while(i.hasNext()) {
			file = i.next();
			isSpaceMatched = code
					.bitwiseAND(mask)
					.bitwiseAND(file.getDbMask())
					.equals(file.getDbUcode().bitwiseAND(mask)
							.bitwiseAND(file.getDbMask())); 
			if(isSpaceMatched) break;
		}
		// If no data file is matched, return null as the result
		if(!isSpaceMatched) return null; 
		
		// Search for matching Data Entry within selected file (Specification page 12)
		// (queryucode & querymask & ucodemask) equals (ucode & querymask & ucodemask) 
		// and (querymask & ucodemask) equals ucodemask
		Iterator<DataEntry> j = file.getDataEntriesView().iterator();
		DataEntry entry = null;
		DataEntry resolutionEntry = null;
		isSpaceMatched = false;

		while (j.hasNext()) {
			entry = j.next();
			isSpaceMatched = (code.bitwiseAND(mask).bitwiseAND(
					entry.getUcodeMask()).equals(entry.getUcode()
					.bitwiseAND(mask).bitwiseAND(entry.getUcodeMask())))
					&& (mask.bitwiseAND(entry.getUcodeMask()).equals(entry
							.getUcodeMask()));

			if (isSpaceMatched) {

				// Choose any data entry if query attribute is
				// UIDC_ATTR_ANONYMOUS
				if (attribute.equals(QueryAttribute.UIDC_ATTR_ANONYMOUS))
					return entry;

				// Further check if the query attribute matches one in the entry
				else if (attribute.getCode() == entry.getDataAttribute()
						.getCode())
					return entry;

				// Remember entry with UIDC_ATTR_RS for resolution redirect if
				// requested attribute not found
				else if (entry.getDataAttribute().equals(
						DataAttribute.UIDC_ATTR_RS))
					resolutionEntry = entry;
			}

		}
		// If not even a data entry's space is matched: return null, if space is
		// matched but not attribute: redirect to another resolution server (if any)
		return resolutionEntry; // This expression is correct ONLY if
								// 'resolutionEntry' would still be null if no
								// space matched, or space matched but no
								// attribute matched with either UIDC_ATTR_RS
								// entry found or not
		
	}
	
	public final UrpPacket parseQueryPacket(byte[] buffer) {
		// Extract parameters from byte array
		int t = ( buffer[ResUcdQueryField.T.getByteIndex()] << 24 ) | 
				( buffer[ResUcdQueryField.T.getByteIndex() + 1] << 16 ) | 
				( buffer[ResUcdQueryField.T.getByteIndex() + 2] << 8 ) | 
				( buffer[ResUcdQueryField.T.getByteIndex() + 3] );
		
		QueryMode mode = QueryMode.valueOf( (short) ( ( buffer[ResUcdQueryField.QUERY_MODE.getByteIndex()] << 8 ) | 
													( buffer[ResUcdQueryField.QUERY_MODE.getByteIndex() + 1] ) ) );
		
		QueryAttribute attribute = QueryAttribute.valueOf( (short) ( ( buffer[ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex()] << 8 ) | 
																	( buffer[ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex() + 1] ) ) );
		
		short ucodeType = (short) ( (buffer[ResUcdQueryField.UCODE_TYPE.getByteIndex()] << 8 ) | ( buffer[ResUcdQueryField.UCODE_TYPE.getByteIndex() + 1] ) );
		short ucodeLength = (short) ( (buffer[ResUcdQueryField.UCODE_LENGTH.getByteIndex()] << 8 ) | ( buffer[ResUcdQueryField.UCODE_LENGTH.getByteIndex() + 1] ) );
		
		// Construct query packet
		ResUcdQuery packet = new ResUcdQuery(t, mode, attribute, ucodeType, ucodeLength);
		
		
		
		packet.setSerialNumber(buffer[UrpPacket.Field.SERIAL_NO.getByteIndex()]);
		short commandId = (short) ( ( buffer[UrpPacket.Field.OPERATOR_HIGH.getByteIndex()] << 8 ) | buffer[UrpPacket.Field.OPERATOR_HIGH.getByteIndex()] );		
		packet.setCommandId(Command.valueOf(commandId));
		// TODO set all parameters by add setData(buffer, index) for best performance!
		
		return packet;
	}
	
	public final UrpPacket parseRecievePacket(byte[] buffer) {
		// TODO complete this as above fashion
		//UrpPacket packet = new ResUcdRecievePacket();		
		//return packet;
		return null;
	}
	
	
}
