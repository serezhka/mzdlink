package com.github.serezhka.mzdlink.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@ChannelHandler.Sharable
public abstract class MinitouchSocketHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ChannelHandlerContext ctx;

    public void sendGesture(ByteBuf gesture) {
        if (ctx != null) {
            ctx.writeAndFlush(gesture);
        } else gesture.release();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = null;
    }
}
