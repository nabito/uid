package com.dadfha.uid;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import com.dadfha.Utils;
import com.dadfha.uid.ResUcdQuery.QueryAttribute;
import com.dadfha.uid.ResUcdQuery.QueryMode;
import com.dadfha.uid.ResUcdQuery.ResUcdQueryField;
import com.dadfha.uid.ResUcdRecieve.ResolveMode;
import com.dadfha.uid.Ucode.UcodeType;
import com.dadfha.uid.UrpQuery.Command;
import com.dadfha.uid.UrpRecieve.Error;
import com.dadfha.uid.client.UC;
import com.dadfha.uid.server.DataEntry;
import com.dadfha.uid.server.DataEntry.DataAttribute;
import com.dadfha.uid.server.DataEntry.DataType;
import com.dadfha.uid.server.DataFile;
import com.dadfha.uid.server.DataFile.CascadeMode;
import com.dadfha.uid.server.UcodeRD;



/**
 * Simplified ucode Resolution Protocol
 * @author Wirawit
 * Note: This has potential to become generic packet construction classes
 * since it make use of Collection instead of classes attribute for packet's fields 
 */
public class UcodeRP {
	
	private static final UcodeRP object = new UcodeRP();	
	private UcodeRD database;
	private UC client;
	// OPT Can improve to use both ucode and IP address of ucode resolve requesting client as a key
	public final Map<Ucode, Short> forwardPacketMap = new HashMap<Ucode, Short>(); 
	public Thread serverThread;
	public Thread clientThread;
	private UrpRecieve tempRecievePacket;
	
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
	 * @return the client
	 */
	public UC getClient() {
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(UC client) {
		this.client = client;
	}
	
	/**
	 * Get ucode Resolution Database
	 * @return
	 */
	public UcodeRD getDatabase() {
		return database;
	}	
	
	/**
	 * Set ucode Resolution Database
	 * @param database
	 */
	public void setDatabase(UcodeRD database) {
		this.database = database;
	}
	
	/**
	 * Check if query if of RES_UCD command then call an appropriate method and return requested data (if any)
	 * This method is expected to be called on the resolver (server) side. 
	 * 
	 * @param packet
	 * @return Object requested data (if any) or null for nothing
	 * 
	 * Command			Return type
	 * RES_UCD			ResUcdRecieve
	 */
	public final Object processQuery(UrpQuery packet) {
		
		Object returnData = null;
				
		switch( packet.getCommandId() ) {
			case RES_UCD:
				// Cast to ResUcdQuery type to reflect fields format of the command 
				ResUcdQuery ruqPacket = (ResUcdQuery) packet;
				
				// Resolve request ucode(s)				
				DataEntry entry;
				
				// Construct response packet
				ResUcdRecieve rurPacket = new ResUcdRecieve();
				rurPacket.setSerialNumber( (byte) ( ruqPacket.getSerialNumber() + 1 ) );
				rurPacket.setResolveMode( ResolveMode.valueOf( ruqPacket.getQueryMode().getCode() ) );					
				
				try {
					entry = resolveUcodeLocal(ruqPacket.getQueryUcode(), ruqPacket.getQueryMask(), ruqPacket.getQueryAttribute(), ruqPacket.getQueryMode());
					
					// If entry match found, define Recieve Packet
					if(entry != null) {
						rurPacket.setErrorCode(Error.E_UIDC_OK);
						rurPacket.setTTL(entry.getTTL());
						rurPacket.setDataVersion(entry.getDataVersion());
						rurPacket.setDataAttribute(entry.getDataAttribute());
						rurPacket.setDataType(entry.getDataType());
						rurPacket.addResUcdData(entry.getData());
						rurPacket.addMask(entry.getUcodeMask().getLongArray());
					} else { // entry == null means the cascade search is in action
						rurPacket = (ResUcdRecieve) tempRecievePacket;
					}					
					
				} catch (Exception e) {
					// In cases of no data file or data entry matched
					rurPacket.setErrorCode(Error.E_UIDC_NOEXS);
				}

				returnData = rurPacket;
				break;
				
			default: 
				break;
		}
		
		return returnData;
	}

	/**
	 * Check the validity of received packet based on query one.
	 * This method is expected to be called on ucode sender (client) side.
	 * @param packet
	 * @return Object returned data (if any) or null for nothing
	 * 
	 * Command			Return type
	 * RES_UCD			ResUcdRecieve
	 * @throws Exception when returned data does not match the query and is not another resolution server
	 */
	public final Object processRecieve(UrpRecieve recievePacket, UrpQuery queryPacket) throws Exception {
		
		Object returnData = null;		
		
		// Cast query and recieve packet type based on query command
		switch(queryPacket.getCommandId()) {
			case RES_UCD:
				
				ResUcdQuery ruqPacket = (ResUcdQuery) queryPacket;
				ResUcdRecieve rurPacket = (ResUcdRecieve) recievePacket;
				
				// Check if the query attribute matches the returned data atrributes
		        if(rurPacket.getDataAttribute().getCode() == ruqPacket.getQueryAttribute().getCode()) {
		        	returnData = recievePacket; // return the original recieve packet if it contains requested data
		        	
		        // Send query to another resolution server
		        } else if(rurPacket.getDataAttribute() == DataAttribute.UIDC_ATTR_RS) { 
		        
					// Check if ucode Resolution Server address is of supported type
					if (!EnumSet.of(DataType.UIDC_DATATYPE_UCODE_HOST,
							DataType.UIDC_DATATYPE_UCODE_IPV4,
							DataType.UIDC_DATATYPE_UCODE_IPV6).contains(rurPacket.getDataType()))
						throw new RuntimeException("Unsupported ucode Resolution Server address format");
					
					String host = Utils.bytesToUTF8String(rurPacket.getResUcdDataBytes());
				
					resolveUcodeRemote(ruqPacket, host);
		        	
		        // throw Exception if returned data does not match the query and is not another resolution server
		        } else { 
		        	throw new Exception("Return data attribute does not comply with the query");
		        }
				
				break;
			default:
				break;
		}
		
		return returnData;
	}
	
	/**
	 * Resolve ucode for Data Entry based on 3 parameters ucode, ucodemask and queryattribute
	 * The database must be defined before being called.
	 * @param code
	 * @param mask
	 * @param attribute
	 * @param mode
	 * @return DataEntry the matched or default entry or null if cascade query is made
	 * @throws Exception When there is no Data Entry match found
	 * @throws RuntimeException when database is null
	 */
	public final DataEntry resolveUcodeLocal(Ucode code, Ucode mask, QueryAttribute attribute, QueryMode mode) throws Exception {
		// Check if database has properly initialized
		if(database == null) throw new NullPointerException("Database of type UcodeRD must not be null.");
		
		Iterator<DataFile> i = database.getDataFilesView().iterator();
		DataFile file = null;
		DataFile resolvedFile = null;
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
			if(isSpaceMatched) {
				// Remember space matched file
				resolvedFile = file;
				// Check Cascade query mode and supported file
				if(mode == QueryMode.UIDC_RSMODE_CASCADE) {
					if(file.getCascadeMode() == CascadeMode.UIDC_CSC) break;  
					else continue;
				} else {
					break;
				}				
			}
		}
		// If no data file is matched, return null as the result
		if(!isSpaceMatched) return null; 
		
		// Search for matching Data Entry within selected file (Specification page 12)
		// (queryucode & querymask & ucodemask) equals (ucode & querymask & ucodemask) 
		// and (querymask & ucodemask) equals ucodemask
		Iterator<DataEntry> j = resolvedFile.getDataEntriesView().iterator();
		DataEntry entry = null;
		DataEntry resolvedEntry = null;
		isSpaceMatched = false;
		
		// Default alternative server with which another search attempt may succeed
		// OPT this should be defined as constant or file resource
		DataEntry defaultEntry = new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050200L),
				new Ucode(0xffffffffffffffffL, 0xffffffffffffff00L),	
				DataAttribute.UIDC_ATTR_RS,
				(short) 1,
				3600,
				DataType.UIDC_DATATYPE_UCODE_IPV4,
				"192.168.10.1");

		while (j.hasNext()) {
			entry = j.next();
			isSpaceMatched = (code.bitwiseAND(mask).bitwiseAND(
					entry.getUcodeMask()).equals(entry.getUcode()
					.bitwiseAND(mask).bitwiseAND(entry.getUcodeMask())))
					&& (mask.bitwiseAND(entry.getUcodeMask()).equals(entry
							.getUcodeMask()));

			if (isSpaceMatched) {

				// Choose any data entry if query attribute is UIDC_ATTR_ANONYMOUS
				if (attribute.equals(QueryAttribute.UIDC_ATTR_ANONYMOUS)) {
					return entry;

				// Further check if the query attribute matches one in the entry
				} else if (attribute.getCode() == entry.getDataAttribute().getCode()) {
					return entry;

				// Remember entry with UIDC_ATTR_RS for resolution redirect if requested attribute not found
				} else if (entry.getDataAttribute().equals(DataAttribute.UIDC_ATTR_RS)) {
					
					// Check if cascade search mode is requested and the file is supported, do query for the client
					if( (mode == QueryMode.UIDC_RSMODE_CASCADE) && (resolvedFile.getCascadeMode() == CascadeMode.UIDC_CSC) ) {
						resolveUcodeCascade(code, mask, attribute, mode, entry.getStringData());
						resolvedEntry = null;
					} else {
						resolvedEntry = entry;
					}					
					
				}
					
			} // Check matching of ucode space
			
		} // Data Entry traversal
		
		// Finally check for default entry if still find no match
		if(resolvedEntry == null) {
			entry = defaultEntry;
			isSpaceMatched = (code.bitwiseAND(mask).bitwiseAND(
					entry.getUcodeMask()).equals(entry.getUcode()
					.bitwiseAND(mask).bitwiseAND(entry.getUcodeMask())))
					&& (mask.bitwiseAND(entry.getUcodeMask()).equals(entry
							.getUcodeMask()));	
			if(isSpaceMatched) resolvedEntry = defaultEntry;
			else throw new Exception("No entry match found.");
		}
		
		return resolvedEntry; 
		
	}
	
	/**
	 * Cascade resolve ucode on remote ucode Resolution Server. 
	 * Save query packet serial number for later forwarding back reference.
	 * Save current running Thread to block wait for returning of cascade data.
	 * @param code ucode
	 * @param mask ucode mask
	 * @param attribute Query Attribute
	 * @param mode Query Mode
	 * @param host IPv4, IPv6 or host address of ucode Resolution Server
	 * @return
	 */
	public final void resolveUcodeCascade(Ucode code, Ucode mask, QueryAttribute attribute, QueryMode mode, String host) {
		// Check if client has properly initialized
		if(client == null) throw new NullPointerException("Client of type UC must not be null.");
				
		// Construct query packet
		ResUcdQuery ruqPacket = new ResUcdQuery(0, mode, attribute, code.getUcodeType(), (short) 0);
		short serialNumber = (short) new Random().nextInt(256);
		ruqPacket.setSerialNumber((byte) serialNumber);
		
		// Remember Serial Number for later reference
		forwardPacketMap.put(code, serialNumber);

		ruqPacket.setT(Utils.getSecondsFromY2K());
		ruqPacket.setQueryMode(mode);
		ruqPacket.setQueryAttribute(attribute);
		ruqPacket.addQuery(code, mask);
		
		// Send the packet and waiting for reply message
		client.connectAndSend(host, ruqPacket);
		
		// Wait for client thread to receive the data first
		try {
			clientThread.join();
		} catch (InterruptedException e) {
			// do nothing here
		}
		
	}
	
	/**
	 * Resolve ucode in normal client process
	 * @param ruqPacket
	 * @param host
	 */
	public final void resolveUcodeRemote(ResUcdQuery ruqPacket, String host) {
		// Check if client has properly initialized
		if(client == null) throw new NullPointerException("Client of type UC must not be null.");
		
		// Update command send time
		ruqPacket.setT(Utils.getSecondsFromY2K());
		
		// Send the packet and waiting for reply message
		client.connectAndSend(host, ruqPacket);
	}
	
	/**
	 * Set Cascade Recieve Packet for server thread to deliver to lower client 
	 * @param packet
	 */
	public final void returnCascadePacket(UrpRecieve packet) {
		tempRecievePacket = packet;
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
