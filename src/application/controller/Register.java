package application.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

/**
 * The type Register.
 */
public class Register implements Initializable {
    private String Username;

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return Username;
    }

    /**
     * Sets username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        Username = username;
    }

    @FXML
    private TextArea name;
    @FXML
    private TextArea pwd;
    @FXML
    private Button confirm;
    @FXML
    private Text username_re;
    @FXML
    private Text password_re;

    /**
     * Initialize.
     *
     * @param location  the location
     * @param resources the resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        confirm.setOnMouseClicked(event -> {
            try {
                String usernameText = name.getText();
                String passwordText = pwd.getText();
                if (passwordText == null || passwordText.equals("")){
                    password_re.setText("Please set password!");
                }
                Socket sock = new Socket("localhost", 7777);
                    try (InputStream input = sock.getInputStream()) {
                        try (OutputStream output = sock.getOutputStream()) {
                            if (register(input, output, usernameText, passwordText)) {
                                setUsername(usernameText);
                                SwitchToLogin(event);
                            }
                            else {
                                username_re.setText("User Name has been used, please reset one!");
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("IO Exception");
                        sock.close();
                    }
                    sock.close();
                } catch(IOException e){
                    throw new RuntimeException(e);
                }
        });
    }

    /**
     * Switch to login.
     *
     * @param actionEvent the action event
     * @throws IOException the io exception
     */
    public void SwitchToLogin(MouseEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("Login.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    private static boolean register(InputStream input, OutputStream output, String UserName, String Password) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        MyWrite(writer,"register");
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
