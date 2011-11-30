package com.dadfha.uid.server;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.dadfha.uid.ResUcdQuery;
import com.dadfha.uid.ResUcdRecieve;
import com.dadfha.uid.UcodeRP;

public class UcrServerHandler extends SimpleChannelUpstreamHandler {
	
	private final UcodeRP protocol = UcodeRP.getUcodeRP();

    private static final Logger logger = Logger.getLogger(
            UcrServerHandler.class.getName());

    private final AtomicLong transferredBytes = new AtomicLong();

    public long getTransferredBytes() {
        return transferredBytes.get();
    }

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

		// Read data from buffer
		ByteBuffer bb = ((ChannelBuffer) e.getMessage()).toByteBuffer();

		// Get buffer ready for read by reset the readIndex
		bb.clear();
		byte[] buffer = null;
		
		if (bb.hasArray()) {
			buffer = bb.array(); // this method only applicable when ByteBuffer is backed by byte[]
		} else {
			buffer = new byte[bb.capacity()];
			bb.get(buffer, 0, buffer.length);
		}
		
		// Parse data in UCR Protocol:UrpQuery format
		ResUcdQuery queryPacket = (ResUcdQuery) protocol.parseQueryPacket(buffer);
		
		// Process query
		ResUcdRecieve returnPacket = (ResUcdRecieve) protocol.processQuery(queryPacket);
	
		// Return resolved data packet
		ChannelBuffer returnBuffer = ChannelBuffers.wrappedBuffer(returnPacket.pack()); // Wrap return packet byte array
		transferredBytes.addAndGet( returnBuffer.readableBytes() ); // Update the transferred byte
		e.getChannel().write(returnBuffer);		

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
