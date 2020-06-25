package sample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistrController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField loginfield;

    @FXML
    private TextField namefield;

    @FXML
    private TextField phonenumberfield;

    @FXML
    private PasswordField passwordfield;

    @FXML
    private RadioButton btmale;

    @FXML
    private RadioButton btfemale;

    @FXML
    private Button submitbutton;

    @FXML
    void initialize() {

        ToggleGroup group = new ToggleGroup();                      // радио кнопка настройка
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {
                    RadioButton button = (RadioButton) group.getSelectedToggle();
                }
            }
        });

        btmale.setToggleGroup(group);
        btmale.setSelected(true);
        btfemale.setToggleGroup(group);
        submitbutton.setOnAction(actionEvent -> {
            try {
                if (loginfield.getText().trim().isEmpty()||namefield.getText().trim().isEmpty()||   //  обработка ошибки пустых полей
                        phonenumberfield.getText().trim().isEmpty()||passwordfield.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Fields are empty");
                    alert.setHeaderText(null);
                    alert.setContentText("One or more fields are empty");
                    alert.showAndWait();
                }
                else{
                    String login = loginfield.getText();
                    String name = namefield.getText();
                    String password = passwordfield.getText();
                    String phnumber = phonenumberfield.getText();
                    RadioButton button = (RadioButton) group.getSelectedToggle();
                    String gender = button.getText();
                    if(singUpNewUser(login,password,name,phnumber,gender)){
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("You have been registered");
                        alert.setHeaderText(null);
                        alert.setContentText("You may login now");
                        alert.showAndWait();
                        Parent registr = FXMLLoader.load(getClass().getResource("/sample/Fxmls/Login.fxml"));
                        Scene registrpage = new Scene(registr);
                        Stage appStage2 = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                        appStage2.hide();
                        appStage2.setScene(registrpage);
                        appStage2.show();
                    }else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("ERROR");
                        alert.setHeaderText("Sorry, but login already used");
                        alert.setContentText("Please try again");
                        alert.showAndWait();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private boolean singUpNewUser(String login, String password, String name, String phone, String gender) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        String[] login_data = {login,password,name,phone,gender};
        String out = "Code::register,";
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
        if(new String(buffer.array()).trim().equals("accept")){
            return true;
        }else{
            return false;
        }
    }
}