package org.ougen;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: OuGen
 * Discription:
 * Date: 0:14 2019/7/12
 */
public class NioServer {
    public static void main(String[] args) throws IOException {
        new NioServer(9999,1024);
    }
    private int port;
    private ByteBuffer byteBuffer;
    private Charset charset = Charset.forName("utf-8");
    private LinkedBlockingQueue<String> msg_queue = new LinkedBlockingQueue<String>();
    private List<SocketChannel> socketChannel_list = new ArrayList<>();
    public NioServer(int port,int size) throws IOException {
        this.port = port;
        this.byteBuffer = ByteBuffer.allocate(size);
        init();
    }

    private void init() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        Selector selector = Selector.open();
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector,SelectionKey.OP_ACCEPT);
        listener(selector);
    }

    private void listener(Selector selector) throws IOException {
        while (true){
            if (selector.select(3000)>0){
                handler(selector);
            }
        }
    }
    private void handler(Selector selector)  throws IOException{
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        for (SelectionKey key:selectionKeys){
            if (key.isValid() && key.isAcceptable()){
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector,SelectionKey.OP_READ);
                socketChannel_list.add(socketChannel);
            }else if (key.isValid()&& key.isReadable()){
                SocketChannel channel = (SocketChannel) key.channel();
                byteBuffer.clear();
                InetSocketAddress socketAddress = (InetSocketAddress) channel.getRemoteAddress();
                byteBuffer.put(charset.encode(socketAddress.getHostName()+" "+socketAddress.getPort()+":"));
                int count = 0;
                try {
                    count = channel.read(byteBuffer);
                    if (count>0){
                        byteBuffer.flip();
                        for (SocketChannel socketChannel : socketChannel_list){
                            socketChannel.write(byteBuffer);
                            byteBuffer.position(0);
                        }
                        byteBuffer.clear();
                    }
                } catch (IOException e) {
                    socketChannel_list.remove(channel);
                    channel.close();
                    byteBuffer.put(charset.encode("连接断开，退出群聊"));
                    int limit = byteBuffer.limit();
                    byteBuffer.flip();
                    for (SocketChannel socketChannel : socketChannel_list){
                        socketChannel.write(byteBuffer);
                        byteBuffer.position(0);
                        byteBuffer.limit(limit);
                    }
                    byteBuffer.clear();
                }
            }
            selectionKeys.remove(key);
        }
    }
}
