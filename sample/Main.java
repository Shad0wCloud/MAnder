package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;

public class Main extends Application {
    static Aes256Class cripto = new Aes256Class();
    public static Stage javaFXC;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Fxmls/Login.fxml"));
        primaryStage.setTitle("MAnder");
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();
    }


    public static void main(String[] args) throws UnknownHostException {
        launch(args);
        /*String[] temp1 = "/127.0.0.1:7777".split("/");
        String[] temp2 = temp1[1].split(":");
        System.out.println(temp2[0]);*/

    }
}
