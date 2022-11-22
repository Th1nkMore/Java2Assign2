package application.controller;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import application.sockets.client;
/**
 * The type Controller.
 */
public class controller_backup implements Initializable {
    /**
     * mode:
     * 0 -> not start yet
     * 1 -> play with computer
     * 2 -> waiting
     * 3 -> play with player
     */
    private static final int port = 7777;
    private static int mode = 0;
    private static int player = 0;
    Random random = new Random();
    @FXML
    private Button refresh;
    @FXML
    private Text result1;
    @FXML
    private Text result;
    @FXML
    private ChoiceBox<String> playerlist;

    /**
     * Gets playerlist.
     *
     * @return the playerlist
     */
    public ChoiceBox<String> getPlayerlist() {
        return playerlist;
    }

    @FXML
    private Button button_pvp;
    @FXML
    private Button bh;
    @FXML
    private CheckBox check;

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUserName() {
        return UserName;
    }

    /**
     * Sets username.
     *
     * @param userName the username
     */
    public void setUserName(String userName) {
        UserName = userName;
    }

    private String UserName;

    /**
     * Is turn boolean.
     *
     * @return the boolean
     */
    public boolean isTURN() {
        return TURN;
    }

    /**
     * Sets turn.
     *
     * @param TURN the turn
     */
    public void setTURN(boolean TURN) {
        this.TURN = TURN;
    }

    private boolean TURN;

    /**
     * Gets player.
     *
     * @return the player
     */
    public static int getPlayer() {
        return player;
    }

    /**
     * Sets player.
     *
     * @param player the player
     */
    public static void setPlayer(int player) {
        controller_backup.player = player;
    }

    /**
     * Gets mode.
     *
     * @return the mode
     */
    public static int getMode() {
        return mode;
    }

    /**
     * Sets mode.
     *
     * @param mode the mode
     */
    public static void setMode(int mode) {
        controller_backup.mode = mode;
    }

    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;
    private String op;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    @FXML
    private Button sa;
    @FXML
    private Text reminder;
    @FXML
    private Button pc;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;
    private int cnt = 0;

    private static int[][] chessBoard = new int[3][3];
    private static boolean[][] flag = new boolean[3][3];
    private client client = null;

//    private final AnimationTimer timer = new AnimationTimer() {
//        @Override
//        public void handle(long now) {
//            cnt++;
//            try (Socket socket = new Socket("localhost", port)) {
//                try (InputStream inputStream = socket.getInputStream()) {
//                    try (OutputStream outputStream = socket.getOutputStream()) {
//                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//                        MyWrite(writer, "Keep");
//                        MyWrite(writer, getUserName());
//                        MyWrite(writer, String.valueOf(getMode()));
//                        if (getMode() == 0) {
//                            Gson gson = new Gson();
//                            ArrayList<String> waiting = gson.fromJson(reader.readLine(), ArrayList.class);
//                            if (cnt==200) {
//                                setUserName(reminder.getText().substring(7));
////                                   System.out.println("update waiting list!");
//                                getPlayerlist().getItems().remove(0, getPlayerlist().getItems().size());
//                                for (String s : waiting) {
//                                    if (!s.equals(getUserName())) {
//                                        getPlayerlist().getItems().add(s);
//                                    }
//                                }
//                                System.out.println(getPlayerlist().getItems());
//                            }
//                        }
//                        if (getMode() == 2) {
//                            String choice = reader.readLine();
//                            if (choice.equals("find")) {
//                                setMode(3);
//                                setTURN(true);
//                                String opponent = reader.readLine();
//                                result1.setText("Start game with " + opponent + ", you start first.");
//                                chessBoard = new int[3][3];
//                            } else {
//                            }
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                System.err.println("test");
//            }
//        }
//    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        timer.start();
        try {
            this.client = new client();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        check.setOnMouseClicked(event ->
        {
            if (check.isSelected()){
                button_pvp.setVisible(false);
                playerlist.setVisible(false);
                refresh.setVisible(false);
                setMode(2);
                result1.setText("Waiting for Player..");
            }else{
                button_pvp.setVisible(true);
                playerlist.setVisible(true);
                refresh.setVisible(true);
                setMode(0);
            }
        });
        refresh.setOnMouseClicked(event -> {
            this.cnt=999;
        });
        button_pvp.setOnMouseClicked(event -> PlayWith());
        sa.setOnMouseClicked(event -> {
            try {
                SwitchToLogin(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        pc.setOnMouseClicked(event -> {
            startPlayWithPC();
            String remind = "Start game with PC" + (getPlayer()==1 ? ", you start first": ", PC start first");
            result1.setText(remind);
        });
        game_panel.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            try {
                refreshBoard(x, y);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void refreshBoard (int x, int y) throws IOException {
        if (chessBoard[x][y] == EMPTY && mode != 0) {
            BufferedWriter writer = this.client.writer;
            BufferedReader reader = this.client.reader;
            if (getMode() == 1 || (getMode() == 3 && isTURN())) {
                chessBoard[x][y] = getPlayer();
            }
            switch (getMode()) {
                case 1:
                    if (terminate(chessBoard) == 0) {
                        Gson gson = new Gson();
                        MyWrite(writer, "PCGameStart " + getPlayer() + " " + gson.toJson(chessBoard));
                        chessBoard = gson.fromJson(reader.readLine(), int[][].class);
                        int t = terminate(chessBoard);
                        if (t != 0) {
                            setMode(0);
                            MyWrite(writer, "End");
                            if (t == getPlayer()) {
                                result.setText("You win!");
                                MyWrite(writer, "Win");
                            } else if (t == 3) {
                                result.setText("Game draw");
                                MyWrite(writer, "Draw");
                            } else {
                                result.setText("You lose!");
                                MyWrite(writer, "Lose");
                            }
                        } else {
                            MyWrite(writer, "Continue");
                        }
                    } else {
                        setMode(0);
                        MyWrite(writer, "PCGameEnd");
                        int re = terminate(chessBoard);
                        MyWrite(writer, String.valueOf(getUserName()));
                        if (re == 3) {
                            result.setText("Game draw!");
                            MyWrite(writer, "Draw");
                        } else {
                            if (re == getPlayer()) {
                                result.setText("You win!");
                                MyWrite(writer, "Win");
                            } else {
                                result.setText("Lose");
                                MyWrite(writer, "Lose");
                            }
                        }
                    }
                    break;
                case 3:
                    if (terminate(chessBoard) == 0) {
                        MyWrite(writer, "PVPGameStart");
                        MyWrite(writer, String.valueOf(getPlayer()));
                        Gson gson = new Gson();
                        MyWrite(writer, gson.toJson(chessBoard));
                        chessBoard = gson.fromJson(reader.readLine(), int[][].class);
                    } else {
                        MyWrite(writer, "PVPGameEnd");
                        MyWrite(writer, String.valueOf(getPlayer()));
                        MyWrite(writer, String.valueOf(terminate(chessBoard)));
                    }
                    break;
            }
            drawChess();
        }
    }

    private void drawChess () {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        flag[i][j] = true;
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        flag[i][j] = true;
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle (int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
    }

    private void drawLine (int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
    }
    private void startPVP(){
        setUserName(reminder.getText().substring(7));
        try (Socket socket = new Socket("localhost", port)){
            try (InputStream inputStream = socket.getInputStream()){
                try (OutputStream outputStream = socket.getOutputStream()){
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    MyWrite(writer, "PVP_register");
                    MyWrite(writer,getUserName());
                    Gson gson = new Gson();
                    this.playerlist.getItems().remove(0,this.playerlist.getItems().size());
                    ArrayList<String> waiting = gson.fromJson(reader.readLine(), ArrayList.class);
                    for (String s : waiting) {
                        if (!s.equals(getUserName())) {
                            this.playerlist.getItems().add(s);
                        }
                    }
                }
            }
        }catch (IOException e){
            System.err.println("IOException");
        }
    }
    private void PlayWith(){
        try{
            String opponent = getPlayerlist().getValue();
            MyWrite(this.client.writer, "PVP");
            MyWrite(this.client.writer,getUserName());
            MyWrite(this.client.writer,opponent);
            if (this.client.reader.readLine().equals("ok")){
                setMode(3);
                setTURN(false);
                setPlayer(2);
                result1.setText("Start game with "+opponent+", "+opponent+" start first");
            }else {
                result1.setText(opponent+" is not available now");
            }
        }catch (IOException e){
            System.err.println("IOException");
        }
    }
    private void startPlayWithPC(){
        Reset();
        check.setSelected(false);
        setUserName(reminder.getText().substring(7));
        setMode(1);
//        try{
//            BufferedWriter writer = this.client.writer;
//            BufferedReader reader = this.client.reader;
//            writer.write("vsPC");
//            setPlayer(Integer.parseInt(reader.readLine()));
//            System.out.println(getPlayer());
//            if (getPlayer()==2){
//                Gson gson = new Gson();
//                chessBoard = gson.fromJson(reader.readLine(), int[][].class);
//                drawChess();
//            }
//        }catch (IOException e){
//            System.err.println(e);
//        }
    }

    /**
     * Terminate int.
     *
     * @param chessBoard the chess board
     * @return 0 : not terminate         1: player 1 wins         2: player 2 wins         3: game draw
     */
    public static int terminate(int[][] chessBoard){

        //check rows and columns
        for (int i = 0; i < 3; i++) {
            if (chessBoard[0][i]!=0 && chessBoard[0][i]==chessBoard[1][i] && chessBoard[1][i]==chessBoard[2][i]){
                return chessBoard[0][i];
            }
            if (chessBoard[i][0]!=0 && chessBoard[i][0]==chessBoard[i][1] && chessBoard[i][1]==chessBoard[i][2]){
                return chessBoard[i][0];
            }
        }
        if (chessBoard[1][1]!=0 && chessBoard[0][0]==chessBoard[1][1] && chessBoard[1][1]==chessBoard[2][2]){
            return chessBoard[1][1];
        }
        if (chessBoard[1][1]!=0 && chessBoard[2][0]==chessBoard[1][1] && chessBoard[1][1]==chessBoard[0][2]){
            return chessBoard[1][1];
        }
        //check if the game draw
        return checkfull(chessBoard)? 3:0;
    }
    private static boolean checkfull(int[][] chessBoard){
        int num = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (chessBoard[i][j] != 0){
                    num++;
                }
            }
        }
        return num==9;
    }
    private static void MyWrite(BufferedWriter writer, String content) throws IOException {
        writer.write(content+"\n");
        writer.flush();
    }

    /**
     * Switch to log in.
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
        Reset();
    }

    /**
     * Reset.
     */
    public void Reset(){
        setMode(0);
        setPlayer(0);
        result.setText("");
        chessBoard = new int[3][3];
        flag = new boolean[3][3];
        drawChess();
//        System.out.println(base_square.getChildren());
        base_square.getChildren().remove(5,base_square.getChildren().size());
    }

}
