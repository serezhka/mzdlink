package com.github.serezhka.mzdlink.websocket;

import com.github.serezhka.mzdlink.service.RemoteControlService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@SuppressWarnings("Duplicates")
@ChannelHandler.Sharable
@Controller
public class MzdlinkWebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger LOGGER = Logger.getLogger(MzdlinkWebsocketHandler.class);

    private final RemoteControlService remoteControlService;

    private ChannelHandlerContext ctx;

    @Value("${config.mzdlink.path}")
    private String path;

    @Value("${config.mzdlink.port}")
    private int port;

    @Autowired
    public MzdlinkWebsocketHandler(RemoteControlService remoteControlService) {
        this.remoteControlService = remoteControlService;
    }

    @PostConstruct
    public void init() {
        remoteControlService.setDeviceScreenListener(this::sendBase64ByteBuf);

        new Thread(() -> {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .localAddress(new InetSocketAddress(port))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(final SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(
                                        new HttpRequestDecoder(),
                                        new HttpObjectAggregator(65536),
                                        new HttpResponseEncoder(),
                                        new WebSocketServerProtocolHandler(path),
                                        MzdlinkWebsocketHandler.this);
                            }
                        });

                serverBootstrap.bind().sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        remoteControlService.processGesture(msg.content().retain());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        synchronized (this) {
            if (this.ctx == null) {
                this.ctx = ctx;
                LOGGER.info("Client " + ctx.channel().remoteAddress() + " connected to " + ctx.channel().localAddress());
            } else {
                ctx.disconnect();
                LOGGER.info("Client " + ctx.channel().remoteAddress() + " connection to " + ctx.channel().localAddress() + " declined.");
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        synchronized (this) {
            if (this.ctx != null && ctx.channel().id().equals(this.ctx.channel().id())) {
                this.ctx = null;
                LOGGER.info("Client " + ctx.channel().remoteAddress() + " disconnected from " + ctx.channel().localAddress());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Mzdlink exception", cause);
        ctx.close();
    }

    private void sendBase64ByteBuf(ByteBuf message) {
        synchronized (this) {
            if (ctx != null) {
                ChannelFuture channelFuture = ctx.writeAndFlush(new TextWebSocketFrame(Base64.encode(message)));
                if (channelFuture.cause() != null) {
                    channelFuture.cause().printStackTrace();
                }
            }
        }
    }
}
