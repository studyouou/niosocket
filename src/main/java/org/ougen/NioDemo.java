package org.ougen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Author: OuGen
 * Discription:
 * Date: 22:03 2019/7/11
 */
public class NioDemo {
    public static void main(String[] args) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        FileInputStream fileInputStream = new FileInputStream("D:\\handdata\\shares.txt");
        FileChannel channel = fileInputStream.getChannel();
        channel.read(byteBuffer);
        byteBuffer.flip();
        while (byteBuffer.hasRemaining()){
            byte[] a = byteBuffer.array();
            System.out.println(new String(a));
            byteBuffer.clear();
        }

    }
}
