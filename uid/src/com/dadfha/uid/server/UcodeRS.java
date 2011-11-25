package com.dadfha.uid.server;

import java.util.Map;
import java.util.TreeMap;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;


import com.dadfha.uid.Ucode;
import com.dadfha.uid.UcodeMask;
import com.dadfha.uid.UcodeRP;
import com.dadfha.uid.server.DataEntry.DataAttribute;
import com.dadfha.uid.server.DataEntry.DataType;
import com.dadfha.uid.server.DataFile.CascadeMode;

/**
 * ucode Resolution Server
 * @author nabito
 */
public class UcodeRS {
	
	private final Map<Ucode, DataFile> ucodeSpace = new TreeMap<Ucode, DataFile>();
	private final UcodeRP ucrp = UcodeRP.getUcodeRP();
	public static final int SERVER_PORT = 8080;
	
	public UcodeRS() { 
		
		// init server process
		initServer();
		
		// init server data
		DataFile file1 = new DataFile(new Ucode(0x0efffec000000000L, 0x0000000000040000L), new UcodeMask(0xffffffffffffffffL, 0xffffffffffff0000L), CascadeMode.UIDC_NOCSC);
		try {
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050100L),
												new UcodeMask(0xffffffffffffffffL, 0xffffffffffffff00L),	
												DataAttribute.UIDC_ATTR_SS,
												(short) 1,
												0,
												DataType.UIDC_DATATYPE_UCODE_URL,
												"http://www.uidcenter.org") );
			
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050200L),
					new UcodeMask(0xffffffffffffffffL, 0xffffffffffffff00L),	
					DataAttribute.UIDC_ATTR_RS,
					(short) 1,
					3600,
					DataType.UIDC_DATATYPE_UCODE_IPV4,
					"192.168.10.1") );	
			
			file1.addDataEntry( new DataEntry( new Ucode(0x0efffec000000000L, 0x0000000000050300L),
					new UcodeMask(0xffffffffffffffffL, 0xffffffffffffff00L),	
					DataAttribute.UIDC_ATTR_RS,
					(short) 2,
					0,
					DataType.UIDC_DATATYPE_UCODE_IPV4,
					"192.168.10.2") );			
			
		} catch (Exception e) {
			// Return message to user that the the supplied ucode is not supported in this space
		}
		DataFile file2 = new DataFile(new Ucode(0x0efffec000000000L, 0x0000000000050000L), new UcodeMask(0xffffffffffffffffL, 0xffffffffffff0000L), CascadeMode.UIDC_CSC);
		ucodeSpace.put(file1.getDbUcode(), file1);
		ucodeSpace.put(file2.getDbUcode(), file2);
		
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
	
	
}
