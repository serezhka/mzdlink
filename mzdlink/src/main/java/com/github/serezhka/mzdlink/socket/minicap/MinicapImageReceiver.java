package com.github.serezhka.mzdlink.socket.minicap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@ChannelHandler.Sharable
public abstract class MinicapImageReceiver extends SimpleChannelInboundHandler<ByteBuf> {

    private Header header;
    private ByteBuf imageFrame;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        while (msg.readableBytes() > 0) {

            if (header == null) {

                // Read minicap's header
                header = new Header();
                header.setVersion(msg.readByte() & 0xFF);
                header.setSize(msg.readByte() & 0xFF);
                header.setPid((int) msg.readUnsignedIntLE());
                header.setRealWidth((int) msg.readUnsignedIntLE());
                header.setRealHeight((int) msg.readUnsignedIntLE());
                header.setVirtualWidth((int) msg.readUnsignedIntLE());
                header.setVirtualHeight((int) msg.readUnsignedIntLE());
                header.setOrientation(msg.readByte() & 0xFF);
                header.setQuirk(msg.readByte() & 0xFF);

                onHeaderReceived(header);

            } else if (imageFrame == null) {

                // Read image frame size and allocate new image buffer
                if (msg.readableBytes() >= 4)
                    //imageFrame = ByteBuffer.allocate((int) msg.readUnsignedIntLE());
                    imageFrame = ByteBufAllocator.DEFAULT.buffer((int) msg.readUnsignedIntLE()); // FIXME default, heap or direct

            } else {

                // Read image frame
                int bytesToRead = Math.min(imageFrame.writableBytes(), msg.readableBytes());
                msg.readBytes(imageFrame, bytesToRead);

                if (!imageFrame.isWritable()) {
                    onReceive(imageFrame);
                    imageFrame = null;
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        header = null;
        imageFrame = null;
    }

    public abstract void onReceive(ByteBuf imageFrame);

    public abstract void onHeaderReceived(Header header);
}
