import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class ChatClient {
    public static void main(String[] args) throws IOException{
        new ChatClient().start();
    }

    ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
    ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    public void start() throws IOException {
        // ��socketͨ��
        SocketChannel sc = SocketChannel.open();
        //����Ϊ������
        sc.configureBlocking(false);
        //���ӷ�������ַ�Ͷ˿�
        sc.connect(new InetSocketAddress("localhost", 8001));
        //��ѡ����
        Selector selector = Selector.open();
        //ע�����ӷ�����socket�Ķ���
        sc.register(selector, SelectionKey.OP_CONNECT);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            //ѡ��һ���������Ӧ��ͨ����Ϊ I/O ����׼��������
            //�˷���ִ�д�������ģʽ��ѡ�������
            selector.select();
            //���ش�ѡ��������ѡ�������
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                // �жϴ�ͨ�����Ƿ����ڽ������Ӳ�����
                if (key.isConnectable()) {
                    sc.finishConnect();
                    sc.register(selector, SelectionKey.OP_READ);
                }
                if (key.isWritable()) { //д����
                    System.out.print("please input message:");
                    String message = scanner.nextLine();
                    //ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
                    writeBuffer.clear();
                    writeBuffer.put(message.getBytes());
                    //������������־��λ,��Ϊ������put�����ݱ�־���ı�Ҫ����ж�ȡ���ݷ��������,��Ҫ��λ
                    writeBuffer.flip();
                    sc.write(writeBuffer);

                    //ע��д����,ÿ��chanelֻ��ע��һ�����������ע���һ����Ч
                    //�����Բ�ֹһ���¼�����Ȥ����ô�����á�λ�򡱲�������������������
                    //int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
                    //ʹ��interest����
                    //sc.register(selector, SelectionKey.OP_READ);
                    //sc.register(selector, SelectionKey.OP_WRITE);
                    //sc.register(selector, SelectionKey.OP_READ);

                }
                if (key.isReadable()){//��ȡ����
                    SocketChannel client = (SocketChannel) key.channel();
                    //������������Ա��´ζ�ȡ
                    readBuffer.clear();
                    int num = client.read(readBuffer);
                    System.out.println(new String(readBuffer.array(),0, num));
                }
            }
        }
    }

}
