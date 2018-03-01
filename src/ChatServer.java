import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChatServer {
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(2048);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(2048);
    private String str;
    private Set<SocketChannel> clientSet = new HashSet<>();

    public void start() throws IOException, InterruptedException {
        selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("localhost",8001));
        //服务器通道只注册监听连接的事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (!Thread.currentThread().isInterrupted()) {
            selector.select();
            Set<SelectionKey> keysSet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keysSet.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (!key.isValid()) {
                    continue;
                } else if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
                iterator.remove();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException, InterruptedException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = ssc.accept();
        clientChannel.configureBlocking(false);
        writeBuffer.clear();
        writeBuffer.put("welcome!".getBytes());
        writeBuffer.flip();
        clientChannel.write(writeBuffer);
        broadCast("a new client connected "+clientChannel.getRemoteAddress());
        //注册读客户端内容的事件
        clientChannel.register(selector, SelectionKey.OP_READ);
        clientSet.add(clientChannel);
    }

    private void write(SelectionKey key) throws IOException, ClosedChannelException {
        SocketChannel channel = (SocketChannel) key.channel();
        System.out.println("write:"+str);

        writeBuffer.clear();
        writeBuffer.put(str.getBytes());
        writeBuffer.flip();
        channel.write(writeBuffer);
        channel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        this.readBuffer.clear();
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            key.cancel();
            socketChannel.close();
            return;
        }
        str = new String(readBuffer.array(), 0, numRead);
        broadCast(socketChannel.getRemoteAddress()+":"+str);
    }

    private void broadCast(String msg){
        //广播
        writeBuffer.clear();
        writeBuffer.put(msg.getBytes());
        writeBuffer.flip();
        clientSet.forEach(client->{
            try {
                client.write(writeBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new ChatServer().start();
    }

}
