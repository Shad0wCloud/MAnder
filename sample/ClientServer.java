package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import sample.Main;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class ClientServer implements Runnable {
    static int confirmed = -1;
    static int id_send = -1;
    static MainpostController MController;
    static DirectController DController;
    @Override
    public void run() {
        try {
            System.out.println("Server have started");
            Selector selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", 7770));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (key.isAcceptable()) { //принимает коннект и выделяем канал
                        connection_accept(selector, serverSocket); //пулл
                    }
                    try { //принимает сообщение от юзера и обрабатывает
                        if (key.isReadable()) {
                            Answer(key);
                        }
                    } catch (IOException e) {
                        key.channel().close();
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    iter.remove();
                }
            }
        }catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void Answer(SelectionKey key) throws IOException, ClassNotFoundException, InterruptedException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
        client.read(MainBuffer);
        Cripto.Get_Decripted(MainBuffer);
        String[] output = new String(MainBuffer.array()).split(","); //принимаем код, айди юзера, имя юзера
        System.out.println("Got msg");
        System.out.println(output[0]);
        if(output[0].equals("code::Signal")){
            MainpostController.check = true;
            id_send = Integer.parseInt(output[1]);
            while (confirmed == -1){
                Thread.sleep(1000);
            }
            if(confirmed == 1){
                confirmed =-1;
                System.out.println(id_send + " it's id send");
            }else{
                confirmed =-1;
                MainpostController.check=false;
            }
        }else if(output[0].equals("code::Offline")){
            DController.textarea.appendText("Server: ************User is afk************"+"\n");
        }else if(output[0].equals("code::AcceptChat")){
            DController.textarea.appendText("Server: ************User accepted your chat invite************"+"\n");
        }else if(output[0].equals("code::DeniedChat")){
            DController.textarea.appendText("Server: ************User denied your chat invite************"+"\n");
        }else if(output[0].equals("code::MsgGet")){
            String msg = output[3];
            DController.textarea.appendText(MainpostController.name + ": "+msg+"\n");
        }
        key.channel().close();
    }
    private static void connection_accept(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }
    static ByteBuffer cleanBuff(ByteBuffer buffer){
        buffer.clear();
        buffer.put(new byte[4096]);
        buffer.clear();
        return buffer;
    }

}