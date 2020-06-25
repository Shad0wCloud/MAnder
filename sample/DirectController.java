package sample;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DirectController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    public TextArea textarea;

    @FXML
    private TextField msgfield;

    @FXML
    private Button sendbutton;

    @FXML
    private ImageView fotodir;

    @FXML
    private Text nametext;

    @FXML
    private Button backbutton;

    @FXML
    void initialize() throws IOException {
        ClientServer.DController = this;
        FileInputStream input = new FileInputStream("src/sample/1.jpg");
            Image img = new Image(input);
            fotodir.setImage(img);
            nametext.setText(MainpostController.name);
            if(ClientServer.id_send ==-1) {
                System.out.println("Отправил сигнал");
                SendSignal();
            }
        backbutton.setOnAction(actionEvent -> {
            try {
                ClientServer.id_send = -1;
                Parent main2 = FXMLLoader.load(getClass().getResource("/sample/Fxmls/Mainpost.fxml"));
                Scene main2page = new Scene(main2);
                Stage appStage8 = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                appStage8.hide();
                appStage8.setScene(main2page);
                appStage8.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
            sendbutton.setOnAction(e->{
                SocketChannel socketChannel = null;
                try {
                    socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
                    ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
                    String send = "code::MsgGet,"+LoginController.id_user+","+MainpostController.position+","+msgfield.getText()+",";
                    //1 - код, 2 - отправитель, 3 - получатель, 4 - сообщение
                    MainBuffer.put(Cripto.Get_Cript(send));
                    MainBuffer.flip();
                    socketChannel.write(MainBuffer);
                    socketChannel.shutdownOutput();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });

    }
    void SendSignal() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
        ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
        String send = "code::Signal,"+MainpostController.position+","+LoginController.id_user+","; //код, кому, свой айди
        MainBuffer.put(Cripto.Get_Cript(send));
        MainBuffer.flip();
        socketChannel.write(MainBuffer);
        socketChannel.shutdownOutput();
    }
}
