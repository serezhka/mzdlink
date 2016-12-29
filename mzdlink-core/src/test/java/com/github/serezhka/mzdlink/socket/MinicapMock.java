package com.github.serezhka.mzdlink.socket;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class MinicapMock extends Thread {

    private static final int PORT = 1114;

    public static void main(String[] args) throws Exception {
        MinicapMock mock = new MinicapMock();
        mock.start();
    }

    @Override
    public void run() {

        try {

            // Minicap header byte buffer
            ByteBuffer minicapHeaderByteBuffer = ByteBuffer.allocateDirect(24);
            minicapHeaderByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            minicapHeaderByteBuffer.put((byte) 1); // Version
            minicapHeaderByteBuffer.put((byte) 23); // Header size
            minicapHeaderByteBuffer.putInt(666); // Process id
            minicapHeaderByteBuffer.putInt(1920); // Real screen width
            minicapHeaderByteBuffer.putInt(1080); // Real screen height
            minicapHeaderByteBuffer.putInt(800); // Virtual width
            minicapHeaderByteBuffer.putInt(480); // Virtual height
            minicapHeaderByteBuffer.put((byte) 0); // Rotation
            minicapHeaderByteBuffer.put((byte) 2); // Quirks

            // Fake screen image buffer
            BufferedImage fakeScreenImage = new BufferedImage(800, 480, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(fakeScreenImage, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();

            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            ByteBuffer fakeImageByteBuffer = ByteBuffer.allocateDirect(4 + imageBytes.length);
            fakeImageByteBuffer.order(ByteOrder.nativeOrder());
            fakeImageByteBuffer.putInt(imageBytes.length);
            fakeImageByteBuffer.put(imageBytes);

            while (!interrupted()) {

                ServerSocketChannel serverSocketChannel = null;
                SocketChannel socketChannel = null;

                try {
                    serverSocketChannel = ServerSocketChannel.open();
                    serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
                    socketChannel = serverSocketChannel.accept();
                    socketChannel.socket().setSendBufferSize(4096);

                    // Send minicap header
                    minicapHeaderByteBuffer.flip();
                    minicapHeaderByteBuffer.limit(minicapHeaderByteBuffer.capacity());
                    int bytesSent = 0;
                    while (bytesSent < minicapHeaderByteBuffer.capacity()) {
                        bytesSent += socketChannel.write(minicapHeaderByteBuffer);
                        minicapHeaderByteBuffer.compact();
                    }
                    Thread.sleep(50);

                    //noinspection InfiniteLoopStatement
                    while (true) {

                        // Send fake image
                        fakeImageByteBuffer.flip();
                        fakeImageByteBuffer.limit(fakeImageByteBuffer.capacity());
                        bytesSent = 0;
                        while (bytesSent < fakeImageByteBuffer.capacity()) {
                            bytesSent += socketChannel.write(fakeImageByteBuffer);
                            fakeImageByteBuffer.compact();
                        }
                        Thread.sleep(50);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    if (serverSocketChannel != null) serverSocketChannel.close();
                    if (socketChannel != null) socketChannel.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
