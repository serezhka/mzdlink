package com.github.serezhka.mzdlink.socket.minitouch;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@ChannelHandler.Sharable
public abstract class MinitouchGestureSender extends ChannelHandlerAdapter {

    private ChannelHandlerContext ctx;

    public void sendGesture(ByteBuf gesture) {
        if (ctx != null) {
            ctx.writeAndFlush(gesture);
        } else gesture.release();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.ctx = null;
    }
}
