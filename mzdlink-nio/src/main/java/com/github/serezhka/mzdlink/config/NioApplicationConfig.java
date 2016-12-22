package com.github.serezhka.mzdlink.config;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.context.annotation.Bean;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class NioApplicationConfig extends ApplicationConfig {

    @Bean
    public static EventLoopGroup bossGroup() {
        return new NioEventLoopGroup();
    }

    @Bean
    public static EventLoopGroup workerGroup() {
        return new NioEventLoopGroup();
    }

    @Bean
    public static Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return NioServerSocketChannel.class;
    }

    @Bean
    public static Class<? extends SocketChannel> socketChannelClass() {
        return NioSocketChannel.class;
    }
}
