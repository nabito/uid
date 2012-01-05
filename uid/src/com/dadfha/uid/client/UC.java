package com.dadfha.uid.client;

import com.dadfha.Utils;
import com.dadfha.uid.ResUcdQuery;
import com.dadfha.uid.ResUcdQuery.QueryAttribute;
import com.dadfha.uid.ResUcdQuery.QueryMode;
import com.dadfha.uid.ResUcdRecieve;
import com.dadfha.uid.Ucode;
import com.dadfha.uid.Ucode.UcodeType;
import com.dadfha.uid.UcodeRP;
import com.dadfha.uid.UrpPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * The Ubiquitous Communicator
 * @author nabito
 *
 */
public class UC {
	
	private static final UcodeRP protocol = UcodeRP.getUcodeRP();
	private UcrClientHandler clientHandler = new UcrClientHandler(this);
	
	private static final int UCODE_SERVER_PORT = 8080;

	public UC() {}
	 
	public static void main(String[] args) {
		
		UC client = new UC();
		Ucode code = client.readUcode();
		Ucode mask = new Ucode(new long[] { 0xffffffffffffffffL, 0xffffffffffffffffL }, UcodeType.UID_128);
		
		// Construct query packet
		//ResUcdQuery ruqPacket = new ResUcdQuery(QueryMode.UIDC_RSMODE_RESOLUTION, QueryAttribute.UIDC_ATTR_SS, code.getUcodeType());
		
		// ...Or construct the cascade resolution request
		ResUcdQuery ruqPacket = new ResUcdQuery(QueryMode.UIDC_RSMODE_CASCADE, QueryAttribute.UIDC_ATTR_SS, code.getUcodeType());
		
		ruqPacket.addQuery(code, mask);
		
		// Remotely resolve ucode
		protocol.setClient(client);
		protocol.resolveUcodeRemote(ruqPacket, "127.0.0.1");
		
	}
	
	/**
	 * Read ucode either from QR code, RFID Tag, etc.
	 * @return
	 */
	public final Ucode readUcode() {
		return new Ucode(new long[] { 0x0efffec000000000L, 0x0000000000050123L }, UcodeType.UID_128);
	}
	
	/**
	 * Connect to UCR Server
	 * @param host hostname of UCR Server
	 * @param port specify specific port of the connection
	 */
    public final void connectAndSend(final String host, UrpPacket packet) {    	
    	try {
			connectAndSend(InetAddress.getByName(host), packet);
		} catch (UnknownHostException e) {
			// Warn user about unknown host
		}    	
    }	
	
	public final void connectAndSend(InetAddress address, UrpPacket packet) {
    	
		// Save reference to sending packet
		clientHandler.setSendingPacket(packet);

        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        clientHandler);
            }
        });

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(address, UCODE_SERVER_PORT));

        // Wait until the connection is closed or the connection attempt fails.
        future.getChannel().getCloseFuture().awaitUninterruptibly();

        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();		
	}
	
	/**
	 * Do whatever client want to do with resolved data
	 * @param packet
	 */
	public void processReturnData(ResUcdRecieve packet) {
		
		byte[] byteData = packet.getResUcdDataBytes();
		
		
		System.out.println("ucode resolution succeed.");
		
		/*
		String s = "Data Length: " + packet.getMask(1);
		System.out.println(s);
		
		for(byte b : byteData) System.out.println(b);
		*/
		
		switch(packet.getDataType()) {
			case UIDC_DATATYPE_UCODE_128:
			case UIDC_DATATYPE_UCODE_256:
			case UIDC_DATATYPE_UCODE_384:
			case UIDC_DATATYPE_UCODE_512: // parse data to Ucode type until this point
			case UIDC_DATATYPE_UCODE_IPV4:
			case UIDC_DATATYPE_UCODE_IPV6:
			case UIDC_DATATYPE_UCODE_URL:
			case UIDC_DATATYPE_UCODE_HOST:
			case UIDC_DATATYPE_UCODE_EMAIL:
			case UIDC_DATATYPE_UCODE_PHONE:
			case UIDC_DATATYPE_UCODE_TXT: // parse String until this point
			case UIDC_DATATYPE_UCODE_USER:
			default:
				String stringData = Utils.bytesToUTF8String(byteData);
				System.out.println(stringData);				
				break;
		}
	}

	/**
	 * This method get called when ucode resolution failed
	 */
	public void resolveFailed() {
		System.out.println("ucode resolution failed.");
	}
	
	
	
}
	