package Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Main;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

public class Server{
    static Aes256Class cripto = new Aes256Class();
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 7777));
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
                try{ //принимает сообщение от юзера и обрабатывает
                    if (key.isReadable()) {
                        Answer(key);
                    }}catch (IOException e){
                    key.channel().close();
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                iter.remove();
            }
        }
    }
    private static void Answer(SelectionKey key) throws IOException, ClassNotFoundException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
        client.read(MainBuffer);
        if(!new String(MainBuffer.array()).trim().substring(0,12).equals("code::getKey")) {
            Get_Decripted(MainBuffer);
        }
        System.out.println(new String(MainBuffer.array()).trim() + " String from user");
        String[] output = new String(MainBuffer.array()).split(",");
        if(new String(MainBuffer.array()).trim().equals("code::getKey")){                       //отдаем ключ шифрования
            cleanBuff(MainBuffer);
            MainBuffer.put(cripto.secretKey.getEncoded());
            MainBuffer.flip();
            client.write(MainBuffer);
        }
        else if(output[0].equals("Code::register")){                 //регистрация
            cleanBuff(MainBuffer);
            if(DatabaseHandler.singUpUser(output[1],output[2],output[3],output[4],output[5]).equals("accept")){
                String[] temp1 = String.valueOf(client.getRemoteAddress()).split("/");
                String[] temp2 = temp1[1].split(":");
                try {
                    DatabaseHandler.SingUpIp(temp2[0]);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                MainBuffer.put(Get_Cript("accept"));
                MainBuffer.flip();
                client.write(MainBuffer);
            }else{
                MainBuffer.put(Get_Cript("denied"));
                MainBuffer.flip();
                client.write(MainBuffer);
            }
            cleanBuff(MainBuffer);
        }else if (output[0].equals("Code::login")){             //логинимся
            cleanBuff(MainBuffer);
            if (DatabaseHandler.CheckLogin(output[1],output[2])) {
                String[] temp1 = String.valueOf(client.getRemoteAddress()).split("/");
                String[] temp2 = temp1[1].split(":");
                try {
                    DatabaseHandler.ChangeIp(temp2[0],DatabaseHandler.GetId(output[1],output[2]));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String str = "accept," +String.valueOf(DatabaseHandler.GetId(output[1],output[2]))+",";
                MainBuffer.put(Get_Cript(str));
                MainBuffer.flip();
                client.write(MainBuffer);
                client.shutdownOutput();
            }else{
                MainBuffer.put(Get_Cript("denied,"));
                MainBuffer.flip();
                client.write(MainBuffer);
                client.shutdownOutput();
            }
            cleanBuff(MainBuffer);
        }else if(output[0].equals("code::nextPost")){           //смена поста
            int position = Integer.parseInt(output[1]);
            int id_user = Integer.parseInt(output[2]);
            DatabaseHandler dbHandler = new DatabaseHandler();
            int n = dbHandler.Size();
            if(position>n) {
                position = 1;
                if(position == id_user){//исключение, если айди = 1
                    position++;
                }
            }
            String info = dbHandler.PostInfo(position);
            String img = dbHandler.Getfoto(position);
            System.out.println(img + "its img");
            info+=position+"!!razdel!!";
            ByteBuffer TempBuffer = ByteBuffer.allocate(4096);
            TempBuffer.put(Get_Cript(info));
            TempBuffer.flip();
            client.write(TempBuffer);
            FileInputStream fis = new FileInputStream(new File("src/Server/img/No_photo.png"));
            try {
                fis = new FileInputStream(new File(img));
            }catch (FileNotFoundException e){
                System.out.println("Img of user was't founded");
            }
            ByteBuffer ImgBuf = ByteBuffer.wrap(fis.readAllBytes());
            fis.close();
            ImgBuf.flip();
            ImgBuf.clear();
            client.write(ImgBuf);
            client.shutdownOutput();
        }else if(output[0].equals("Code::GetInfo")){             //получаем инфу про юзера
            int id = Integer.parseInt(output[1]);
            String profile = DatabaseHandler.getUser(id);
            cleanBuff(MainBuffer);
            MainBuffer.put(Get_Cript(profile));
            MainBuffer.flip();
            client.write(MainBuffer);
            String img = DatabaseHandler.Getfoto(id);
            FileInputStream fis = new FileInputStream(new File(img));
            ByteBuffer ImgBuf = ByteBuffer.wrap(fis.readAllBytes());
            fis.close();
            ImgBuf.flip();
            ImgBuf.clear();
            client.write(ImgBuf);
            client.shutdownOutput();
        }else if(output[0].equals("Code::UpdateInfo")){ //Photo update delete
            String[] info = new String(MainBuffer.array()).trim().split("!!razdel!!");
            int id = Integer.parseInt(info[6]);
            DatabaseHandler.ProfileInfo(info[1],info[2],info[3],String.valueOf(info[5]),info[4],
                    "src/server/img/"+id+".jpg",id);
        }else if(output[1].equals("code::NewImg")){
            int id = Integer.parseInt(output[2]);
            ByteBuffer tempBuff = ByteBuffer.allocate(4096);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (client.read(tempBuff) != -1) {
                tempBuff.flip();
                while (tempBuff.hasRemaining())
                    baos.write(tempBuff.get());
                tempBuff.clear();
            }
            FileOutputStream fos = new FileOutputStream(new File("src/Server/img/"+id+".jpg"));
            fos.flush();
            fos.write(baos.toByteArray());
            fos.close();
            baos.close();
        }else if(output[0].equals("code::Like")){
            int value = Integer.parseInt(output[2]);
            int id = Integer.parseInt(output[1]);
            DatabaseHandler.GetLike(id, value);
        }else if(output[0].equals("code::GetDirect")){
            int id = Integer.parseInt(output[1]);
            String[] info = DatabaseHandler.getUser(id).split(",");
            cleanBuff(MainBuffer);
            MainBuffer.put(Get_Cript(info[0]));
            MainBuffer.flip();
            client.write(MainBuffer);
            client.shutdownOutput();
        }else if(output[0].equals("code::Signal")){
            int id_recieve = Integer.parseInt(output[1]);
            int id_send = Integer.parseInt(output[2]);
            try {
                String[] ips = DatabaseHandler.GetIps(id_send,id_recieve);
                try {
                    SocketChannel sender = SocketChannel.open(new InetSocketAddress(ips[2],Integer.parseInt(ips[3])));
                    cleanBuff(MainBuffer);
                    MainBuffer.put(Get_Cript("code::Signal,"+id_send+","));
                    MainBuffer.flip();
                    sender.write(MainBuffer);
                    sender.shutdownOutput();
                }catch (ConnectException e){
                    System.out.println("no connection,");
                    SocketChannel offline = SocketChannel.open(new InetSocketAddress(ips[0], Integer.parseInt(ips[1])));
                    cleanBuff(MainBuffer);
                    MainBuffer.put(Get_Cript("code::Offline,"));
                    MainBuffer.flip();
                    offline.write(MainBuffer);
                    offline.shutdownOutput();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if(output[0].equals("code::AcceptChat")){
            int id = Integer.parseInt(output[1]);
            System.out.println("he will recive id - " + id);
            try {
                String[] ips = DatabaseHandler.GetIps(1,id);
                SocketChannel sender = SocketChannel.open(new InetSocketAddress(ips[2],Integer.parseInt(ips[3])));
                cleanBuff(MainBuffer);
                MainBuffer.put(Get_Cript("code::AcceptChat,"));
                MainBuffer.flip();
                sender.write(MainBuffer);
                sender.shutdownOutput();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if(output[0].equals("code::DeniedChat")){
            int id = Integer.parseInt(output[1]);
            try {
                String[] ips = DatabaseHandler.GetIps(1,id);
                SocketChannel sender = SocketChannel.open(new InetSocketAddress(ips[2],Integer.parseInt(ips[3])));
                cleanBuff(MainBuffer);
                MainBuffer.put(Get_Cript("code::DeniedChat,"));
                MainBuffer.flip();
                sender.write(MainBuffer);
                sender.shutdownOutput();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if(output[0].equals("code::MsgGet")){
            int id_senter = Integer.parseInt(output[1]);
            int id_reciver = Integer.parseInt(output[2]);
            String msg = output[3];
            try {
                String[] ips = DatabaseHandler.GetIps(id_senter,id_reciver);
                SocketChannel sender = SocketChannel.open(new InetSocketAddress(ips[2],Integer.parseInt(ips[3])));
                cleanBuff(MainBuffer);
                MainBuffer.put(Get_Cript("code::MsgGet,"+id_senter+","+id_reciver+","+msg+","));
                //1 - код, 2 - отправитель, 3 - получатель, 4 - сообщение
                MainBuffer.flip();
                sender.write(MainBuffer);
                sender.shutdownOutput();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
    static ByteBuffer Get_Decripted(ByteBuffer MainBuffer){
        MainBuffer.flip();
        ByteArrayOutputStream TempBaos = new ByteArrayOutputStream();
        while (MainBuffer.hasRemaining()){
            TempBaos.write(MainBuffer.get());
        }
        byte[] randomrandom = TempBaos.toByteArray();
        byte[] temp = Cripto.Decript(randomrandom,cripto);
        cleanBuff(MainBuffer);
        MainBuffer.put(temp);
        MainBuffer.flip();
        MainBuffer.clear();
        return MainBuffer;
    }
    static byte[] Get_Cript(String msg){
        return Cripto.Crypt(msg,cripto);
        //return msg.getBytes();
    }
}
