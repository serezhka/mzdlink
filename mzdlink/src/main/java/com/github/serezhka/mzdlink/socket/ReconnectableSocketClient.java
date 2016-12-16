package com.github.serezhka.mzdlink.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;

import java.net.SocketAddress;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class ReconnectableSocketClient extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ReconnectableSocketClient.class);

    private final SocketAddress socketAddress;
    private final int reconnectDelay;
    private final ChannelHandler channelHandler;
    private final NioEventLoopGroup workerGroup;

    public ReconnectableSocketClient(SocketAddress socketAddress,
                                     int reconnectDelay,
                                     ChannelHandler channelHandler,
                                     NioEventLoopGroup workerGroup) {
        this.socketAddress = socketAddress;
        this.reconnectDelay = reconnectDelay;
        this.channelHandler = channelHandler;
        this.workerGroup = workerGroup;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            Bootstrap bootstrap = new Bootstrap();
            try {
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(socketAddress)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(channelHandler);
                            }
                        });
                LOGGER.info("Connecting to " + socketAddress);
                ChannelFuture channelFuture = bootstrap.connect().sync();
                LOGGER.info("Connected to " + socketAddress);
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                if (e instanceof InterruptedException) return;
                LOGGER.debug(e);
            } finally {
                LOGGER.info("Disconnected from " + socketAddress);
            }

            try {
                sleep(reconnectDelay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
