package com.dadfha.uid.server;


import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.dadfha.uid.Ucode;
import com.dadfha.uid.server.DataEntry.DataAttribute;
import com.dadfha.uid.server.DataEntry.DataType;
import com.dadfha.uid.server.DataFile.CascadeMode;

/**
 * ucode Resolution Server
 * @author nabito
 */
public class UcodeRS {
	
	private UcodeRD database;
	public static final int SERVER_PORT = 8080;
	
	public UcodeRS() { 						
		database = new UcodeRD();
	}
	
	public static void main(String[] args) {
		UcodeRS server = new UcodeRS();
		server.initServer();
		server.initMockDbData();
	}
	
	public final void initServer() {
				
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new UcrServerHandler());
            }
        });

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(SERVER_PORT));
						
	}
	
	/**
	 * Initialize mock-up db data
	 */
	public void initMockDbData() {
		
		DataFile file1 = new DataFile(new Ucode(0x0efffec000000000L, 0x0000000000040000L), new Ucode(0xffffffffffffffffL, 0xffffffffffff0000L), CascadeMode.UIDC_NOCSC);
		try {
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050100L),
												new Ucode(0xffffffffffffffffL, 0xffffffffffffff00L),	
												DataAttribute.UIDC_ATTR_SS,
												(short) 1,
												0,
												DataType.UIDC_DATATYPE_UCODE_URL,
												"http://www.uidcenter.org") );
			
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050200L),
					new Ucode(0xffffffffffffffffL, 0xffffffffffffff00L),	
					DataAttribute.UIDC_ATTR_RS,
					(short) 1,
					3600,
					DataType.UIDC_DATATYPE_UCODE_IPV4,
					"192.168.10.1") );	
			
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050300L),
					new Ucode(0xffffffffffffffffL, 0xffffffffffffff00L),	
					DataAttribute.UIDC_ATTR_RS,
					(short) 2,
					0,
					DataType.UIDC_DATATYPE_UCODE_IPV4,
					"192.168.10.2") );			
			
		} catch (Exception e) {
			// Return message to user that the the supplied ucode is not supported in this space
		}
		DataFile file2 = new DataFile(new Ucode(0x0efffec000000000L, 0x0000000000050000L), new Ucode(0xffffffffffffffffL, 0xffffffffffff0000L), CascadeMode.UIDC_CSC);
		database.addDataFile(file1);
		database.addDataFile(file2);				
		
	}	
	
	/**
	 * This function get called when query process failed.
	 */
	public void queryProcessFailed() {
		
	}
	
}
