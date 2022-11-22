package application.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class Login implements Initializable{
    private String Username;

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    @FXML
    private TextArea name;
    @FXML
    private TextArea pwd;
    @FXML
    private Button button;
    @FXML
    private Hyperlink register;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        button.setOnMouseClicked(event -> {
            try {
                String UserName = name.getText();
                String Password = pwd.getText();
                Socket sock = new Socket("localhost", 7777);
                try (InputStream input = sock.getInputStream()) {
                    try (OutputStream output = sock.getOutputStream()) {
                        if (login(input, output, UserName, Password)){
                            setUsername(UserName);
                            SwitchToMain(event);
                        }
                    }
                } catch (IOException e){
                    System.err.println("IO Exception");
                    sock.close();
                }
                sock.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        register.setOnMouseClicked(event -> {
            try {
                SwitchToRegister(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void SwitchToMain(MouseEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();

        fxmlLoader.setLocation(getClass().getClassLoader().getResource("Play.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Node child = root.getChildrenUnmodifiable().get(2);
        Text text = (Text) child;
        text.setText("Hello, "+ getUsername());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void SwitchToRegister(MouseEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("Register.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    private static boolean login(InputStream input, OutputStream output, String UserName, String Password) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        MyWrite(writer,"login");
        MyWrite(writer,UserName);
        MyWrite(writer,Password);
        String response = reader.readLine();
        writer.close();
        reader.close();
        return response.equals("true");
    }
    private static void MyWrite(BufferedWriter writer, String content) throws IOException {
        writer.write(content+"\n");
        writer.flush();
    }
}
