package com.github.serezhka.mzdlink.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.SocketAddress;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Component
public class ReconnectableSocketFactory {

    private static final Logger LOGGER = Logger.getLogger(ReconnectableSocketFactory.class);

    private final EventLoopGroup workerGroup;
    private final Class<? extends SocketChannel> socketChannelClass;

    @Autowired
    public ReconnectableSocketFactory(EventLoopGroup workerGroup,
                                      Class<? extends SocketChannel> socketChannelClass) {
        this.workerGroup = workerGroup;
        this.socketChannelClass = socketChannelClass;
    }

    public Thread connect(SocketAddress socketAddress, int reconnectDelay, ChannelHandler channelHandler) {
        return new Thread(() -> {
            while (!Thread.interrupted()) {
                Bootstrap bootstrap = new Bootstrap();
                try {
                    bootstrap.group(workerGroup)
                            .channel(socketChannelClass)
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
                    Thread.sleep(reconnectDelay);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
    }
}