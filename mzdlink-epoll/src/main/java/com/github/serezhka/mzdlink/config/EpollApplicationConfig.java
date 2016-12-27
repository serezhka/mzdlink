package com.github.serezhka.mzdlink.config;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import org.springframework.context.annotation.Bean;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class EpollApplicationConfig extends ApplicationConfig {

    /*static {
        System.setProperty("io.netty.eventLoopThreads", "4");
        System.setProperty("io.netty.allocator.numDirectArenas", "4");
        System.setProperty("io.netty.allocator.numHeapArenas", "4");
    }*/

    @Bean
    public static EventLoopGroup bossGroup() {
        return new EpollEventLoopGroup();
    }

    @Bean
    public static EventLoopGroup workerGroup() {
        return new EpollEventLoopGroup();
    }

    @Bean
    public static Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return EpollServerSocketChannel.class;
    }

    @Bean
    public static Class<? extends SocketChannel> socketChannelClass() {
        return EpollSocketChannel.class;
    }
}
