package com.dadfha.uid;

import java.util.Arrays;
import java.util.Iterator;

import com.dadfha.Utils;
import com.dadfha.uid.ResUcdQuery.QueryAttribute;
import com.dadfha.uid.ResUcdQuery.QueryMode;
import com.dadfha.uid.ResUcdQuery.ResUcdQueryField;
import com.dadfha.uid.ResUcdRecieve.ResolveMode;
import com.dadfha.uid.Ucode.UcodeType;
import com.dadfha.uid.UrpQuery.Command;
import com.dadfha.uid.UrpRecieve.Error;
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
		
		ResUcdQuery ruqPacket = (ResUcdQuery) packet;
		ResUcdRecieve rurPacket = null;
		
		// Construct response packet
		rurPacket = new ResUcdRecieve();
		rurPacket.setSerialNumber( (byte) ( Utils.ubyteToInt( ruqPacket.getSerialNumber() ) + 1 ) );
		rurPacket.setResolveMode( ResolveMode.valueOf( ruqPacket.getQueryMode().getCode() ) );		
		
		switch( packet.getCommandId() ) {
			case RES_UCD:
				
				// Resolve request ucode(s)				
				 DataEntry entry = resolveUcode(ruqPacket.getQueryUcode(), ruqPacket.getQueryMask(), ruqPacket.getQueryAttribute());
					
				// Set error code
				if(entry != null) {
					rurPacket.setErrorCode(Error.E_UIDC_OK);
					rurPacket.setTTL(entry.getTTL());
					rurPacket.setDataVersion(entry.getDataVersion());
					rurPacket.setDataAttribute(entry.getDataAttribute());
					rurPacket.setDataType(entry.getDataType());
					rurPacket.addResUcdData(entry.getData());
					rurPacket.addMask(entry.getUcodeMask().getBitsArray());
				} else { // In case entry is null (Indicating either no data file or data entry matched)
					rurPacket.setErrorCode(Error.E_UIDC_NOEXS);
				}		
				break;
				
			default: 
				break;
		}
		
		return rurPacket;
	}
	
	/**
	 * Resolve ucode for Data Entry based on 3 parameters ucode, ucodemask and queryattribute
	 * The database must be defined before being called.
	 * @param code
	 * @param mask
	 * @param attribute
	 * @return DataEntry
	 * @throws RuntimeException when database is null
	 */
	public final DataEntry resolveUcode(Ucode code, Ucode mask, QueryAttribute attribute) {
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
	
	/**
	 * Parse query packet from byte buffer
	 * @param buffer
	 * @return UrpPacket with ResUcdQuery object
	 */
	public final UrpPacket parseQueryPacket(byte[] buffer) {
		
		// OPT set ALL parameters by buffer copy technique for best performance!
		
		// Extract parameters from byte array
		int t = ( buffer[ResUcdQueryField.T.getByteIndex()] << 24 ) | 
				( buffer[ResUcdQueryField.T.getByteIndex() + 1] << 16 ) | 
				( buffer[ResUcdQueryField.T.getByteIndex() + 2] << 8 ) | 
				( buffer[ResUcdQueryField.T.getByteIndex() + 3] );
		
		QueryMode mode = QueryMode.valueOf( (short) ( ( buffer[ResUcdQueryField.QUERY_MODE.getByteIndex()] << 8 ) | 
													( buffer[ResUcdQueryField.QUERY_MODE.getByteIndex() + 1] ) ) );
		
		QueryAttribute attribute = QueryAttribute.valueOf( (short) ( ( buffer[ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex()] << 8 ) | 
																	( buffer[ResUcdQueryField.QUERY_ATTRIBUTE.getByteIndex() + 1] ) ) );
		
		UcodeType ucodeType = UcodeType.valueOf( (short) ( (buffer[ResUcdQueryField.UCODE_TYPE.getByteIndex()] << 8 ) | ( buffer[ResUcdQueryField.UCODE_TYPE.getByteIndex() + 1] ) ) );
				
		short ucodeLength = (short) ( (buffer[ResUcdQueryField.UCODE_LENGTH.getByteIndex()] << 8 ) | ( buffer[ResUcdQueryField.UCODE_LENGTH.getByteIndex() + 1] ) );
		
		// Construct query packet
		ResUcdQuery packet = new ResUcdQuery(t, mode, attribute, ucodeType, ucodeLength);
				
		packet.setSerialNumber(buffer[UrpPacket.Field.SERIAL_NO.getByteIndex()]);
		short commandId = (short) ( ( buffer[UrpPacket.Field.OPERATOR_HIGH.getByteIndex()] << 8 ) | buffer[UrpPacket.Field.OPERATOR_HIGH.getByteIndex()] );		
		packet.setCommandId(Command.valueOf(commandId));
		
		// Add queryucode/querymask from buffer assuming the code and mask are always of the same size
		int queryUcodeSize = ( buffer.length - ResUcdQueryField.QUERY_UCODE.getByteIndex() ) / 2;
		int ucodeLastByteIndex = ( ResUcdQueryField.QUERY_UCODE.getByteIndex() + queryUcodeSize ) - 1; 
		packet.addQueryUcode(Arrays.copyOfRange(buffer, ResUcdQueryField.QUERY_UCODE.getByteIndex(), ucodeLastByteIndex));
		packet.addQueryMask(Arrays.copyOfRange(buffer, ucodeLastByteIndex + 1, buffer.length - 1));
		
		return packet;
	}
	
	/**
	 * Parse recieve packet from byte buffer
	 * @param buffer
	 * @return UrpPacket with ResUcdRecieve object
	 */	
	public final UrpPacket parseRecievePacket(byte[] buffer) {		
		UrpPacket packet = new ResUcdRecieve(buffer);				
		return packet;
	}
	
	
}
