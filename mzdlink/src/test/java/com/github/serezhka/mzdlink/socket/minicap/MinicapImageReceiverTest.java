package com.github.serezhka.mzdlink.socket.minicap;

import com.github.serezhka.mzdlink.socket.ReconnectableSocketClient;
import io.netty.buffer.ByteBuf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class MinicapImageReceiverTest extends JFrame {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 1114;

    private BufferedImage image;

    public MinicapImageReceiverTest() {

        SocketAddress socketAddress = new InetSocketAddress(HOST, PORT);

        MinicapImageReceiver minicapImageReceiver = new MinicapImageReceiver() {
            @Override
            public void onReceive(ByteBuf byteBuffer) {
                byte[] bytes = new byte[byteBuffer.readableBytes()];
                byteBuffer.readBytes(bytes);
                try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                    synchronized (MinicapImageReceiverTest.this) {
                        image = ImageIO.read(inputStream);
                    }
                    MinicapImageReceiverTest.this.repaint();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onHeaderReceived(Header header) {
                System.err.println("Minicap's header received: " + header);
            }
        };

        ReconnectableSocketClient minicapClient = new ReconnectableSocketClient(socketAddress, 2000, minicapImageReceiver);
        minicapClient.start();

        setSize(800, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        getContentPane().add(new JPanel() {

            @Override
            public void paint(Graphics g) {
                try {
                    synchronized (MinicapImageReceiverTest.this) {
                        if (image != null) {
                            //g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
                            image.flush();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        new MinicapImageReceiverTest();
    }
}