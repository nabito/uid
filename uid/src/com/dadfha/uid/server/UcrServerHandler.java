package com.dadfha.uid.server;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.dadfha.Utils;
import com.dadfha.uid.ResUcdQuery;
import com.dadfha.uid.ResUcdRecieve;
import com.dadfha.uid.UcodeRP;

public class UcrServerHandler extends SimpleChannelUpstreamHandler {
	
	private final UcodeRP protocol = UcodeRP.getUcodeRP();
	private UcodeRS server = null;

    private static final Logger logger = Logger.getLogger(
            UcrServerHandler.class.getName());

    private final AtomicLong transferredBytes = new AtomicLong();

    public long getTransferredBytes() {
        return transferredBytes.get();
    }

	/**
	 * @return the server
	 */
	public UcodeRS getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(UcodeRS server) {
		this.server = server;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		
		// Save reference to server thread
    	protocol.serverThread = Thread.currentThread();		
		
		// Update the transferred byte
		transferredBytes.addAndGet(((ChannelBuffer) e.getMessage()).readableBytes()); 

		// Read data from buffer
		byte[] buffer = Utils.byteBufferToByteArray(((ChannelBuffer) e.getMessage()).toByteBuffer());
		
		// Parse data in UCR Protocol:UrpQuery format
		ResUcdQuery queryPacket = (ResUcdQuery) protocol.parseQueryPacket(buffer);
		
		// Process query
		Object rawData = protocol.processQuery(queryPacket);
		
		if(rawData == null) {
			server.queryProcessFailed();
			return;
		} else {	
			// Return resolved data packet
			ResUcdRecieve returnPacket = (ResUcdRecieve) rawData;
			ChannelBuffer returnBuffer = ChannelBuffers.wrappedBuffer(returnPacket.pack()); // Wrap return packet byte array
			e.getChannel().write(returnBuffer);					
		}
		
		// ??? close connection here?
		
	}

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }
}
