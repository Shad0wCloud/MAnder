package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainpostController {
    static boolean check = false;
    boolean likePress =false;
    static int position = 0;
    static String name = "person";
    static int CurLikes = 0;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ImageView img;

    @FXML
    private TextArea infoprofilefield;

    @FXML
    private Button Editprofilebutton;


    @FXML
    private Button directbutton;

    @FXML
    private Button likebutton;

    @FXML
    private Button nextbutton;

    @FXML
    private Text like;


    @FXML
    void initialize() {
        ClientServer.MController = this;
        directbutton.setOpacity(0);
        likebutton.setOnAction(actionEvent -> {          //лайк под фото
            int likes = Integer.parseInt(like.getText());
            if(likePress){
                likes--;
                like.setText(String.valueOf(likes));
                likePress = false;
            }else{
                likes++;
                like.setText(String.valueOf(likes));
                likePress = true;
            }
            try {
                GiveLike();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        directbutton.setOnAction(actionEvent -> {
            try {
                GetName(position);
                Parent direct = FXMLLoader.load(getClass().getResource("/sample/Fxmls/Direct.fxml"));
                Scene directpage = new Scene(direct);
                Stage appStage5 = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                appStage5.hide();
                appStage5.setScene(directpage);
                appStage5.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Editprofilebutton.setOnAction(actionEvent -> {
            try {
                Parent profile = FXMLLoader.load(getClass().getResource("/sample/Fxmls/Profile.fxml"));
                Scene profilepage = new Scene(profile);
                Stage appStage6 = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                appStage6.hide();
                appStage6.setScene(profilepage);
                appStage6.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        nextbutton.setOnAction(actionEvent -> {
            likePress = false;
            directbutton.setOpacity(1);
            try {
                position++;
                if(position == LoginController.id_user){ //скипаем себя
                    position++;
                }
                System.out.println(position+ " - it's position");
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
                ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
                String send = "code::nextPost,"+position+","+LoginController.id_user+",";
                MainBuffer.put(Cripto.Get_Cript(send));
                MainBuffer.flip();
                socketChannel.write(MainBuffer);
                socketChannel.shutdownOutput();
                ByteBuffer InputBuffer = ByteBuffer.allocate(4096);
                socketChannel.read(InputBuffer);
                Cripto.Get_Decripted(InputBuffer);
                System.out.println(new String(MainBuffer.array()).trim());
                String info[] = new String(InputBuffer.array()).trim().split("!!razdel!!");
                position = Integer.parseInt(info[2]);
                like.setText(info[1]);
                infoprofilefield.setText(info[0]); //получили инфу и занесем ее
                ByteBuffer tempBuff = ByteBuffer.allocate(4096);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (socketChannel.read(tempBuff) != -1) {
                    tempBuff.flip();
                    while (tempBuff.hasRemaining())
                        baos.write(tempBuff.get());
                    tempBuff.clear();
                }
                FileOutputStream fos = new FileOutputStream(new File("src/sample/1.jpg"));
                fos.flush();
                fos.write(baos.toByteArray());
                fos.close();
                baos.close();
                FileInputStream input = new FileInputStream("src/sample/1.jpg");
                img.setImage(new Image(input));
                img.setFitWidth(337);
                img.setFitHeight(489);
                input.close();
                if(check){
                    check = false;
                    ShowAlertSignal();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    static void GetName(int id) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
        ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
        String send ="code::GetDirect," + id +",";
        MainBuffer.put(Cripto.Get_Cript(send));
        MainBuffer.flip();
        socketChannel.write(MainBuffer);
        socketChannel.shutdownOutput();
        MainBuffer.clear();
        MainBuffer.put(new byte[4096]);
        MainBuffer.clear();
        socketChannel.read(MainBuffer);
        Cripto.Get_Decripted(MainBuffer);
        System.out.println(new String(MainBuffer.array()).trim());
        String[] out = new String(MainBuffer.array()).trim().split("!!razdel!!");
        name = out[0];
    }
    static void ShowAlertSignal() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("We got him");
        alert.setHeaderText("Look, Someone want to chat with you");
        alert.setContentText("Would you like to chat with user?");
        ButtonType buttonTypeOne = new ButtonType("Enter the chat");
        ButtonType buttonTypeCancel = new ButtonType("Decline", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        position = ClientServer.id_send-1;
        if (result.get() == buttonTypeOne){
            ClientServer.confirmed = 1;
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
            ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
            int id_sent = position+1;
            System.out.println("it's our id_sent " + id_sent);
            String send ="code::AcceptChat," + id_sent +",";
            MainBuffer.put(Cripto.Get_Cript(send));
            MainBuffer.flip();
            socketChannel.write(MainBuffer);
            ClientServer.MController.nextbutton.fire();
            ClientServer.MController.directbutton.fire();
        } else {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
            ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
            int id_sent = position+1;
            System.out.println("it's our id_sent " + id_sent);
            String send ="code::DeniedChat," + id_sent +",";
            MainBuffer.put(Cripto.Get_Cript(send));
            MainBuffer.flip();
            socketChannel.write(MainBuffer);
            ClientServer.confirmed = 0;
        }
    }
    public void GiveLike() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
        ByteBuffer MainBuffer = ByteBuffer.allocate(4096);
        String send ="code::Like," + position +","+like.getText()+",";
        MainBuffer.put(Cripto.Get_Cript(send));
        MainBuffer.flip();
        socketChannel.write(MainBuffer);
        socketChannel.shutdownOutput();
    }
}

