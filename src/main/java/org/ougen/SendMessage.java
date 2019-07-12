package org.ougen;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * Author: OuGen
 * Discription:
 * Date: 12:09 2019/7/12
 */
public class SendMessage extends Thread {
    private SocketChannel socketChannel ;
    private ByteBuffer byteBuffer ;
    private boolean quit = false;

    public void setQuit(boolean quit) {
        this.quit = quit;
    }

    public SendMessage(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        byteBuffer = ByteBuffer.allocate(1024);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true){
            if (quit) {
                break;
            }
            String msg = scanner.nextLine();
            if ("exit".equals(msg))break;
            byteBuffer.put(msg.getBytes());
            byteBuffer.flip();
            try {
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
            } catch (IOException e) {
            }
        }

    }
}
