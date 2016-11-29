package com.github.serezhka.mzdlink.openstf;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public abstract class MinitouchGestureSender extends ReconnectableSocketClient {

    public MinitouchGestureSender(SocketAddress socketAddress, int reconnectDelay, int bufferSize) {
        super(socketAddress, reconnectDelay, bufferSize);
    }

    public void sendGesture(byte[] gesture) {
        synchronized (byteBuffer) {
            if (byteBuffer.remaining() > gesture.length)
                byteBuffer.put(gesture);
            byteBuffer.notify();
        }
    }

    @Override
    protected void onConnect(SocketChannel socketChannel) throws Exception {

        while (!interrupted()) {
            synchronized (byteBuffer) {

                while (byteBuffer.position() == 0)
                    byteBuffer.wait();

                byteBuffer.flip();

                if (socketChannel.write(byteBuffer) < 0)
                    break;

                byteBuffer.compact();
            }
        }
    }

    @Override
    protected void onDisconnect() {
    }
}
