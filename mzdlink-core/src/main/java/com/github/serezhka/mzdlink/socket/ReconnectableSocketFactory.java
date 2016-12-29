package com.github.serezhka.mzdlink.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

import java.net.SocketAddress;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class ReconnectableSocketFactory {

    private final EventLoopGroup workerGroup;
    private final Class<? extends SocketChannel> socketChannelClass;

    public ReconnectableSocketFactory(EventLoopGroup workerGroup,
                                      Class<? extends SocketChannel> socketChannelClass) {
        this.workerGroup = workerGroup;
        this.socketChannelClass = socketChannelClass;
    }

    public Thread connect(SocketAddress socketAddress, int bufferSize, int reconnectDelay, ChannelHandler channelHandler) {
        return new Thread(() -> {
            while (!Thread.interrupted()) {
                Bootstrap bootstrap = new Bootstrap();
                try {
                    bootstrap.group(workerGroup)
                            .channel(socketChannelClass)
                            .remoteAddress(socketAddress)
                            .option(ChannelOption.SO_RCVBUF, bufferSize)
                            .option(ChannelOption.SO_SNDBUF, bufferSize)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(channelHandler);
                                }
                            });
                    ChannelFuture channelFuture = bootstrap.connect().sync();
                    channelFuture.channel().closeFuture().sync();
                } catch (Exception e) {
                    if (e instanceof InterruptedException) return;
                }

                try {
                    Thread.sleep(reconnectDelay);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
    }
}