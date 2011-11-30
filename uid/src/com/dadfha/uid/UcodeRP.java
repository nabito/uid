package com.dadfha.uid;

import java.net.InetAddress;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.dadfha.uid.ResUcdQuery.QueryAttribute;
import com.dadfha.uid.ResUcdQuery.QueryMode;
import com.dadfha.uid.ResUcdQuery.ResUcdQueryField;
import com.dadfha.uid.UrpQuery.Command;

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
			for(UcodeType t : EnumSet.allOf(UcodeType.class)) {
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
	
	private static final UcodeRP object = new UcodeRP();;
	
	private UcodeRP() {
	}
	
	public static UcodeRP getUcodeRP() {
		return object;
	}
	
	public final byte[] resolveUcode(Ucode code, InetAddress address) {
		// TODO  resolveUcode()
		
		return null;
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
		
		ResUcdQuery packet = new ResUcdQuery(t, mode, attribute, ucodeType, ucodeLength);
		packet.setSerialNumber(buffer[UrpPacket.Field.SERIAL_NO.getByteIndex()]);
		short commandId = (short) ( ( buffer[UrpPacket.Field.OPERATOR_HIGH.getByteIndex()] << 8 ) | buffer[UrpPacket.Field.OPERATOR_HIGH.getByteIndex()] );		
		packet.setCommandId(Command.valueOf(commandId));
		// TODO set all parameters
		// ??? Considering add setData(buffer, index) for performance improvement!
		
		return packet;
	}
	
	public final UrpPacket parseRecievePacket(byte[] buffer) {
		// TODO complete this as above fashion
		//UrpPacket packet = new ResUcdRecievePacket();		
		//return packet;
		return null;
	}
	
	public final void processRequest() {
		// ??? done after the parse?
	}
	
	
}
