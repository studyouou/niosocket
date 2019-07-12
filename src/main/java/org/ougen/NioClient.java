package org.ougen;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Set;

/**
 * Author: OuGen
 * Discription:
 * Date: 0:15 2019/7/12
 */
public class NioClient {
    private int port;
    private ByteBuffer byteBuffer ;
    private Charset charset = Charset.forName("utf-8");
    private SendMessage sendMessage;
    public NioClient(int port,int size) throws IOException {
        this.port = port;
        this.byteBuffer = ByteBuffer.allocate(size);
        init();
    }

    private void init() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress("localhost",port));
        listener(selector,socketChannel);
    }

    private void listener(Selector selector,SocketChannel socketChannel) throws IOException {
        sendMessage = new SendMessage(socketChannel);
        sendMessage.start();
        while (true){
            if (selector.select(3000)>0){
                handler(selector);
            }
        }
    }

    private void handler(Selector selector) throws IOException {
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        for (SelectionKey key : selectionKeys){
            if (key.isValid()&&key.isConnectable()){
                SocketChannel socketChannel = (SocketChannel) key.channel();
                if (socketChannel.isConnectionPending()){
                    socketChannel.finishConnect();
                    System.out.println("请输入你的昵称(按两下回车哟):");
                    Scanner scanner = new Scanner(System.in);
                    String nickName = scanner.nextLine();
                    byteBuffer.clear();
                    byteBuffer.put(charset.encode(nickName));
                    byteBuffer.flip();
                    socketChannel.write(byteBuffer);
                }
                socketChannel.configureBlocking(false);
                socketChannel.register(selector,SelectionKey.OP_READ);
            }else if (key.isValid()&&key.isReadable()) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                try {
                    byteBuffer.clear();
                    int count = socketChannel.read(byteBuffer);
                    String msg = null;
                    if (count>0){
                        byteBuffer.flip();
                        msg = String.valueOf(charset.decode(byteBuffer).array());
                        System.out.println(msg);
                    }
                }catch (IOException e){
                    socketChannel.close();
                    sendMessage.setQuit(true);
                    throw new RuntimeException("");
                }
            }
            selectionKeys.remove(key);
        }
    }

    public static void main(String[] args) {
        try {
            new NioClient(9999,1024);
        } catch (Exception e) {
            System.out.println("服务器异常,按回车结束");
        }
    }
}
