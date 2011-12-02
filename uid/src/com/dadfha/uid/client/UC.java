package com.dadfha.uid.client;

import com.dadfha.uid.UcodeRP;

import java.net.InetSocketAddress;
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
	
	private UcodeRP protocol = UcodeRP.getUcodeRP();

	public UC() {
		
	}
	
	public static void main(String[] args) {
		
	}
	
	// TODO make support for InetAddress connection and add the sendUcode, readUcodeTag and mock-up main 
	/**
	 * Connect to UCR Server
	 * @param host hostname of UCR Server
	 * @param port specify specific port of the connection
	 * @param firstMessageSize pass 0 if not known in advance (256 will be used default)
	 * @throws Exception
	 */
    public final void connect(final String host, final int port, int firstMessageSize) throws Exception {

    	// Check if first message size is defined and finalize it
    	final int messageSize;
        if(firstMessageSize == 0) messageSize = 256;
        else messageSize = firstMessageSize;

        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new UcrClientHandler(messageSize));
            }
        });

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        // Wait until the connection is closed or the connection attempt fails.
        future.getChannel().getCloseFuture().awaitUninterruptibly();

        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();
    }	
	
}
	