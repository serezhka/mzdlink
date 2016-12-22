package com.github.serezhka.mzdlink.websocket;

import com.github.serezhka.mzdlink.service.RemoteControlService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@ChannelHandler.Sharable
@Component
public class MzdlinkWebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final RemoteControlService remoteControlService;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Class<? extends ServerSocketChannel> serverSocketChannelClass;

    private ChannelHandlerContext ctx;

    @Value("${config.mzdlink.path}")
    private String path;

    @Value("${config.mzdlink.port}")
    private int port;

    @Autowired
    public MzdlinkWebsocketHandler(RemoteControlService remoteControlService,
                                   EventLoopGroup bossGroup,
                                   EventLoopGroup workerGroup,
                                   Class<? extends ServerSocketChannel> serverSocketChannelClass) {
        this.remoteControlService = remoteControlService;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.serverSocketChannelClass = serverSocketChannelClass;
    }

    @PostConstruct
    public void init() {
        remoteControlService.setDeviceScreenListener(this::sendBase64ByteBuf);

        UniqueConnectionFilter connectionFilter = new UniqueConnectionFilter();

        new Thread(() -> {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            try {
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(serverSocketChannelClass)
                        .localAddress(new InetSocketAddress(port))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(final SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(
                                        connectionFilter,
                                        new HttpRequestDecoder(4096, 8192, 8192, false),
                                        new HttpObjectAggregator(65536),
                                        new HttpResponseEncoder(),
                                        new WebSocketServerProtocolHandler(path),
                                        MzdlinkWebsocketHandler.this);
                            }
                        });

                serverBootstrap.bind().sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        remoteControlService.processGesture(msg.content().retain());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.ctx = null;
    }

    private void sendBase64ByteBuf(ByteBuf message) {
        if (ctx != null) {
            ctx.executor().execute(() -> ctx.writeAndFlush(new TextWebSocketFrame(Base64.encode(message))));
        } else message.release();
    }
}
