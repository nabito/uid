package com.dadfha.uid.client;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.dadfha.Utils;	
import com.dadfha.uid.ResUcdQuery;
import com.dadfha.uid.ResUcdRecieve;
import com.dadfha.uid.UcodeRP;
import com.dadfha.uid.UrpQuery;


public class UcrClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(
    		UcrClientHandler.class.getName());

    private final AtomicLong transferredBytes = new AtomicLong();
    
    private UcodeRP protocol = UcodeRP.getUcodeRP();
    private UC client = null;
    private Object sendingPacket = null;

    /**
     * Creates a client-side handler.
     */    
    public UcrClientHandler(UC client) {
    	this.client = client;
    }
    
    public final void setSendingData(Object data) {
    	this.sendingPacket = data;
    }
    
    public final Object getSendingData() {
    	return sendingPacket;
    }
    
    public final UC getUcClient() {
    	return client;
    }
    
    public final void setUcClient(UC client) {
    	this.client = client;
    }

    public final long getTransferredBytes() {
        return transferredBytes.get();
    }

    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) {
        // ??? Send the packet when connected
    	if(sendingPacket == null) throw new RuntimeException("The packet has yet to be defined.");
        e.getChannel().write(sendingPacket);
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {
    	
		// Save reference to client thread
    	protocol.clientThread = Thread.currentThread();
    	    	
    	// Update the transferred byte
    	transferredBytes.addAndGet(((ChannelBuffer) e.getMessage()).readableBytes());
    	
        // Extract the packet, hand it to UC client        
    	byte[] buffer = Utils.byteBufferToByteArray(((ChannelBuffer) e.getMessage()).toByteBuffer());       
    	
    	// Parse received packet
        ResUcdRecieve returnPacket = (ResUcdRecieve) protocol.parseRecievePacket(buffer);
        ResUcdQuery queryPacket = null;
        
        // Process returned packet
        if(!(sendingPacket instanceof UrpQuery)) throw new RuntimeException("The sending packet is not of type UrpQuery.");
        else {
        	
        	// Cast based on command type of the query packet
        	UrpQuery uq = (UrpQuery) sendingPacket;
        	switch(uq.getCommandId()) {
	        	case RES_UCD:
	        		queryPacket = (ResUcdQuery) sendingPacket;
	        		break;
	        	default:
	        		break;
        	}
        	
        }
        
        Object rawData = null;
        
		try {
			rawData = protocol.processRecieve(returnPacket, queryPacket);
		} catch (Exception ex) {
			client.resolveFailed();
		}

		// If the rawData is null after processRecieve() just ignore the packet
        if(rawData == null) {
        	return;
        } else { // If it is not null hands the whole packet to client
        	ResUcdRecieve finalPacket = (ResUcdRecieve) rawData;
        	client.processReturnData(finalPacket);        	
        	// Check if this is cascade requested packet that need to be forwarded back
        	if(protocol.forwardPacketMap.remove(queryPacket.getQueryUcode()) + 1 == finalPacket.getSerialNumber()) {
        		protocol.returnCascadePacket(finalPacket);
        	}
        	
        }        
        
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