package application.controller;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class History implements Initializable {
    Gson gson = new Gson();
    @FXML
    private TableColumn<Record, Integer> game_id;
    @FXML
    private TableColumn<Record, String> opponent;
    @FXML
    private TableColumn<Record, String> result;
    @FXML
    private Button back;
    @FXML
    private Button refresh;
    @FXML
    private TableView<Record> table;
    @FXML
    private Text username;
    public ObservableList<Record> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game_id.setCellValueFactory(new PropertyValueFactory<Record, Integer>("game_id"));
        opponent.setCellValueFactory(new PropertyValueFactory<Record, String>("opponent"));
        result.setCellValueFactory(new PropertyValueFactory<Record, String>("result"));
        refresh.setOnMouseClicked(event -> {
            try {
                String UserName = username.getText();
                Socket sock = new Socket("localhost", 7777);
                try (InputStream input = sock.getInputStream()) {
                    try (OutputStream output = sock.getOutputStream()) {
                        String response = history(input, output, UserName);
                        ArrayList<String> get = gson.fromJson(response, ArrayList.class);
                        list.remove(0,list.size());
                        for (String s : get){
                            String[] msg = s.split("\\s+");
                            list.add(new Record(Integer.parseInt(msg[0]), msg[1], msg[2]));
                        }
                    }
                } catch (IOException e){
                    System.err.println("IO Exception");
                    sock.close();
                }
                sock.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }        });
        back.setOnMouseClicked(event -> {
            try {
                SwitchToMain(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        table.setItems(this.list);
    }
    private static String history(InputStream input, OutputStream output, String UserName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        MyWrite(writer,"History");
        MyWrite(writer,UserName);
        return reader.readLine();
    }
    private static void MyWrite(BufferedWriter writer, String content) throws IOException {
        writer.write(content+"\n");
        writer.flush();
    }
    public void SwitchToMain(MouseEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();

        fxmlLoader.setLocation(getClass().getClassLoader().getResource("Play.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Node child = root.getChildrenUnmodifiable().get(2);
        Text text = (Text) child;
        text.setText("Hello, "+ username.getText());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
