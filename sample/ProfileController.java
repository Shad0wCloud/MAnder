package sample;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
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
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ProfileController {
    static int age = -1;
    String[] info;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Pane fotopane;


    @FXML
    private void selectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать фото");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Картинки", "*.jpg", "*.png", "*.gif", "*.bmp");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(Main.javaFXC);
        Image im = new Image(file.toURI().toString());
        ImageView imv = new ImageView(im);
        imv.setFitHeight(244);
        imv.setFitWidth(168);
        fotopane.getChildren().add(imv);
        System.out.println(im.getUrl().substring(6));
        try {
            SendNewPhoto(im.getUrl().substring(6));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private TextField namefield;

    @FXML
    private TextField countryfield;

    @FXML
    private TextField cityfield;

    @FXML
    private TextField agefield;

    @FXML
    private TextArea infofield;

    @FXML
    private Button savebutton;

    @FXML
    private Button backbutton;

    @FXML
    void initialize() {
        info = GetInfo();
        namefield.setText(info[0]);
        countryfield.setText(info[1]);
        cityfield.setText(info[2]);
        agefield.setText(info[3]);
        infofield.setText(info[4]);
        backbutton.setOnAction(actionEvent -> {
            try {
                Parent main1 = FXMLLoader.load(getClass().getResource("/sample/Fxmls/Mainpost.fxml"));
                Scene main1page = new Scene(main1);
                Stage appStage7 = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                appStage7.hide();
                appStage7.setScene(main1page);
                appStage7.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        savebutton.setOnAction(actionEvent -> {
            try {
                UpdateInfo(namefield.getText(),countryfield.getText(),cityfield.getText(),Integer.parseInt(agefield.getText()),infofield.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public static String[] GetInfo(){
        String[] output = {};
        try {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            String str ="Code::GetInfo,"+LoginController.id_user+",";
            buffer.put(Cripto.Get_Cript(str));
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
            buffer.put(new byte[4096]);
            buffer.clear();
            socketChannel.read(buffer);
            Cripto.Get_Decripted(buffer);
            output = new String(buffer.array()).trim().split("!!razdel!!");
            buffer.flip();
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
    public static void UpdateInfo(String name, String country, String city, int age, String info) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        String out = "Code::UpdateInfo,!!razdel!!"
                +name+"!!razdel!!"
                +country+"!!razdel!!"
                +city+"!!razdel!!"
                +info+"!!razdel!!"
                +age+"!!razdel!!"
                +LoginController.id_user+"!!razdel!!";
        buffer.put(Cripto.Get_Cript(out));
        buffer.flip();
        socketChannel.write(buffer);
    }
    public static void SendNewPhoto(String src) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
        ByteBuffer msgBuff = ByteBuffer.allocate(4096);
        String out = "code::getKey,code::NewImg," +LoginController.id_user+",";
        msgBuff.put(out.getBytes());
        msgBuff.flip();
        msgBuff.clear();
        socketChannel.write(msgBuff);
        FileInputStream fis = new FileInputStream(new File(src));
        ByteBuffer ImgBuf = ByteBuffer.wrap(fis.readAllBytes());
        fis.close();
        ImgBuf.flip();
        ImgBuf.clear();
        socketChannel.write(ImgBuf);
        socketChannel.shutdownOutput();
    }
    /*
    public void GetPic(){
        try {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
            ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
            MainBuffer.put("code::GetProfile".getBytes());
            MainBuffer.put(",".getBytes());
            ByteBuffer tempBuff = ByteBuffer.allocate(4096);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.out.println("info done");
            while (socketChannel.read(tempBuff) != -1) {
                System.out.println("im working");
                tempBuff.flip();
                while (tempBuff.hasRemaining())
                    baos.write(tempBuff.get());
                tempBuff.clear();
            }
            FileOutputStream fos = new FileOutputStream(new File("src/sample/profilePhoto.jpg"));
            fos.flush();
            fos.write(baos.toByteArray());
            fos.close();
            baos.close();
            Image im = new Image("sample/profilePhoto.jpg");
            ImageView imv = new ImageView(im);
            imv.setFitHeight(244);
            imv.setFitWidth(168);
            fotopane.getChildren().add(imv);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}
