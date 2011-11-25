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
	
	// TODO modify client code to be more generic
    public final void connect(String[] args) throws Exception {
        // Print usage if no argument is specified.
        if (args.length < 2 || args.length > 3) {
            System.err.println(
                    "Usage: " + UC.class.getSimpleName() +
                    " <host> <port> [<first message size>]");
            return;
        }

        // Parse options.
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        final int firstMessageSize;
        if (args.length == 3) {
            firstMessageSize = Integer.parseInt(args[2]);
        } else {
            firstMessageSize = 256;
        }

        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new UcrClientHandler(firstMessageSize));
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
