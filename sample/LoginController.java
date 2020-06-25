package sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class LoginController {
    static int id_user = -1;
    static String login_user ="Person";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private PasswordField passwordfield;

    @FXML
    private TextField loginfield;

    @FXML
    private Button registrbutton;

    @FXML
    private Button loginbutton;

    @FXML
    void initialize() {
        registrbutton.setOnAction(actionEvent -> {
            try {
                SetKey();
                Parent login = FXMLLoader.load(getClass().getResource("/sample/Fxmls/Registr.fxml"));
                Scene loginpage = new Scene(login);
                Stage appStage1 = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                appStage1.hide();
                appStage1.setScene(loginpage);
                appStage1.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        loginbutton.setOnAction(actionEvent -> {
            try {
                if (loginfield.getText().isEmpty() || passwordfield.getText().isEmpty()){//пустые поля
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Fields are empty");
                    alert.setContentText("One or more fields are empty");
                    alert.showAndWait();
                }else{
                    SetKey();
                    if(LoginUser(loginfield.getText(),passwordfield.getText())) {//вход
                        login_user = loginfield.getText();
                        new Thread(new ClientServer()).start();
                        Parent main = FXMLLoader.load(getClass().getResource("/sample/Fxmls/Mainpost.fxml"));
                        Scene mainpage = new Scene(main);
                        Stage appStage3 = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                        appStage3.hide();
                        appStage3.setScene(mainpage);
                        appStage3.show();
                    }else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);//не правильный логин или пароль
                        alert.setTitle("Error");
                        alert.setHeaderText("Login or Password is not correct");
                        alert.setContentText("Please try again");
                        alert.showAndWait();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private boolean LoginUser(String login, String password) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        String[] login_data = {login,password};
        String out = "Code::login,";
        for (String s:login_data) {
            out+=s+",";
        }
        buffer.put(Cripto.Get_Cript(out));
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        buffer.put(new byte[4096]);
        buffer.clear();
        socketChannel.read(buffer);
        Cripto.Get_Decripted(buffer);
        String[] output = new String(buffer.array()).trim().split(",");
        if(output[0].equals("accept")){
            id_user = Integer.parseInt(output[1]);
            return true;
        }else{
            return false;
        }
    }
    public void SetKey() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        buffer.put("code::getKey".getBytes());
        buffer.flip();
        buffer.clear();
        socketChannel.write(buffer);
        socketChannel.shutdownOutput();
        buffer.clear();
        buffer.put(new byte[4096]);
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (buffer.hasRemaining()){
            baos.write(buffer.get());
        }
        SecretKey key2 = new SecretKeySpec(baos.toByteArray(), 0, baos.toByteArray().length, "AES");
        Main.cripto.secretKey = key2;
    }
}

