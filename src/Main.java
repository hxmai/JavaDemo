import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Main {
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(2048);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(2048);
    private String str;
    public void start()throws IOException{
        ServerSocketChannel serverSocketChannel1 = ServerSocketChannel.open();
        serverSocketChannel1.configureBlocking(false);
        serverSocketChannel1.bind(new InetSocketAddress("localhost",8001));
        selector = Selector.open();
        serverSocketChannel1.register(selector, SelectionKey.OP_ACCEPT);
        while (!Thread.currentThread().isInterrupted()) {
            selector.select();
            Set<SelectionKey> set = selector.selectedKeys();
            Iterator<SelectionKey> iterator = set.iterator();
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
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = ssc.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("a new client connected "+clientChannel.getRemoteAddress());
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

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();
//        readBuffer.flip();
        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();

            return;
        }

        str = new String(readBuffer.array(), 0, numRead);
        System.out.println(str);
        socketChannel.register(selector, SelectionKey.OP_WRITE);
    }


    public static void main(String[] args) throws Exception {
        new Main().start();
    }

}
