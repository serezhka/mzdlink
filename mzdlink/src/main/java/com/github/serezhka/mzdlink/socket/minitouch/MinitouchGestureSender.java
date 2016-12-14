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
        synchronized (this) {
            if (ctx != null) {
                ctx.writeAndFlush(gesture);
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        synchronized (this) {
            if (this.ctx == null) {
                this.ctx = ctx;
            } else {
                ctx.disconnect();
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        synchronized (this) {
            if (this.ctx != null && ctx.channel().id().equals(this.ctx.channel().id())) {
                this.ctx = null;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        synchronized (this) {
            ctx.close();
        }
    }
}
