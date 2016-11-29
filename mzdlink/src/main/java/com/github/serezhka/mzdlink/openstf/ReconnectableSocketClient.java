package com.github.serezhka.mzdlink.openstf;

import org.apache.log4j.Logger;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public abstract class ReconnectableSocketClient extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ReconnectableSocketClient.class);

    protected final ByteBuffer byteBuffer;

    private final SocketAddress socketAddress;
    private final int reconnectDelay;
    private final int bufferSize;

    public ReconnectableSocketClient(SocketAddress socketAddress, int reconnectDelay, int bufferSize) {
        this.socketAddress = socketAddress;
        this.reconnectDelay = reconnectDelay;
        this.bufferSize = bufferSize;
        byteBuffer = ByteBuffer.allocateDirect(bufferSize);
        byteBuffer.order(ByteOrder.nativeOrder());
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try (SocketChannel socketChannel = SocketChannel.open()) {
                socketChannel.configureBlocking(true);
                socketChannel.socket().setReceiveBufferSize(bufferSize);
                socketChannel.socket().setKeepAlive(true);
                socketChannel.socket().setReuseAddress(true);
                socketChannel.socket().setSoLinger(false, 0);
                socketChannel.socket().setSoTimeout(0);
                socketChannel.socket().setTcpNoDelay(true);

                LOGGER.info("Connecting to " + socketAddress);
                socketChannel.connect(socketAddress);

                if (socketChannel.finishConnect()) {
                    LOGGER.info("Connected to " + socketAddress);
                    onConnect(socketChannel);
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) return;
                LOGGER.error(e);
            } finally {
                LOGGER.info("Disconnected from " + socketAddress);
                byteBuffer.clear();
                onDisconnect();
            }

            try {
                sleep(reconnectDelay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    protected abstract void onConnect(SocketChannel socketChannel) throws Exception;

    protected abstract void onDisconnect();
}
