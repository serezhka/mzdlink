package com.github.serezhka.mzdlink.config;

import com.github.serezhka.mzdlink.socket.ReconnectableSocketFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import se.vidstige.jadb.JadbConnection;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Configuration
@ComponentScan("com.github.serezhka.mzdlink")
@PropertySource("classpath:mzdlink.properties")
public class ApplicationConfig {

     /*static {
        System.setProperty("io.netty.eventLoopThreads", "4");
        System.setProperty("io.netty.allocator.numDirectArenas", "4");
        System.setProperty("io.netty.allocator.numHeapArenas", "4");
    }*/

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static EventLoopGroup bossGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    @Bean
    public static EventLoopGroup workerGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    @Bean
    public static Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    @Bean
    public static Class<? extends SocketChannel> socketChannelClass() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    @Bean
    public static JadbConnection jadbConnection() {
        return new JadbConnection();
    }

    @Bean
    public static ReconnectableSocketFactory reconnectableSocketFactory(EventLoopGroup workerGroup,
                                                                        Class<? extends SocketChannel> socketChannelClass) {
        return new ReconnectableSocketFactory(workerGroup, socketChannelClass);
    }
}
