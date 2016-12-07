package com.github.serezhka.mzdlink.socket.minicap;

import com.github.serezhka.mzdlink.socket.ReconnectableSocketClient;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public abstract class MinicapImageReceiver extends ReconnectableSocketClient {

    private Header header;
    private ByteBuffer imageFrame;

    public MinicapImageReceiver(SocketAddress socketAddress, int reconnectDelay, int bufferSize) {
        super(socketAddress, reconnectDelay, bufferSize);
    }

    @Override
    protected void onConnect(SocketChannel socketChannel) throws IOException {

        int bytesTotalRead;
        while ((bytesTotalRead = socketChannel.read(byteBuffer)) > -1) {

            if (bytesTotalRead > 0) {

                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {

                    if (header == null) {

                        // Read minicap's header
                        try {
                            header = new Header();
                            header.setVersion(byteBuffer.get() & 0xFF);
                            header.setSize(byteBuffer.get() & 0xFF);
                            header.setPid(byteBuffer.getInt());
                            header.setRealWidth(byteBuffer.getInt());
                            header.setRealHeight(byteBuffer.getInt());
                            header.setVirtualWidth(byteBuffer.getInt());
                            header.setVirtualHeight(byteBuffer.getInt());
                            header.setOrientation(byteBuffer.get() & 0xFF);
                            header.setQuirk(byteBuffer.get() & 0xFF);

                            onHeaderReceived(header);

                        } catch (BufferUnderflowException e) {
                            throw new RuntimeException("Buffer too small for minicap's header? " + byteBuffer.capacity(), e);
                        }

                    } else if (imageFrame == null) {

                        // Read image frame size and allocate new image buffer
                        if (byteBuffer.remaining() >= 4)
                            imageFrame = ByteBuffer.allocate(byteBuffer.getInt());

                    } else {

                        // Read image frame
                        int bytesToRead = Math.min(imageFrame.remaining(), byteBuffer.remaining());
                        byteBuffer.get(imageFrame.array(), imageFrame.arrayOffset() + imageFrame.position(), bytesToRead);
                        imageFrame.position(imageFrame.position() + bytesToRead);

                        //while (byteBuffer.hasRemaining() && imageFrame.hasRemaining())
                        //    imageFrame.put(byteBuffer.get());

                        if (!imageFrame.hasRemaining()) {
                            onReceive(imageFrame);
                            imageFrame = null;
                        }
                    }
                }
                byteBuffer.compact();
            }
        }
    }

    @Override
    protected void onDisconnect() {
        header = null;
        imageFrame = null;
    }

    public abstract void onReceive(ByteBuffer byteBuffer);

    public abstract void onHeaderReceived(Header header);
}
