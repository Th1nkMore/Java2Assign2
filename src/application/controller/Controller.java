package application.controller;

import com.google.gson.Gson;
import javafx.animation.AnimationTimer;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import application.sockets.client;
/**
 * The type Controller.
 */
public class Controller implements Initializable {
    Gson gson = new Gson();
    /**
     * mode:
     * 0 -> not start yet
     * 1 -> play with computer
     * 2 -> play with player
     */
    private static final int port = 7777;
    private static int mode = 0;
    Random random = new Random();

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
        Controller.mode = mode;
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
    private Text result1;
    @FXML
    private Text result;
    @FXML
    private Text reminder;
    @FXML
    private Button pc;
    @FXML
    private Pane base_square;
    @FXML
    private ChoiceBox<String> playlist;
    @FXML
    private Button refresh;
    @FXML
    private CheckBox check;
    @FXML
    private Button button_pvp;
    @FXML
    private Button bh;
    @FXML
    private Text turn;
    @FXML
    private Rectangle game_panel;

    private static int[][] chessBoard = new int[3][3];
    private static boolean[][] flag = new boolean[3][3];
    private client client = null;
    private long time;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (isTURN()){
                turn.setText("Your turn!");
            }else {
                turn.setText("");
            }
            if (client.isFound()){
                op = client.OpponentName;
                result1.setText("Start playing with " + op +", you start first");
                client.setPlayer(1);
                setMode(2);
                setTURN(true);
                client.setFound(false);
            }
            if (client.isPlaying()){
                setMode(2);
                int[] res = compare(chessBoard, client.GetChessBoard);
                if (res[0]!=-1){
                    chessBoard = client.GetChessBoard;
                    drawChess();
                    if (chessBoard[res[0]][res[1]]!=0 && chessBoard[res[0]][res[1]]!=client.getPlayer()){
                        setTURN(true);
                    }
                    if (terminate(client.GetChessBoard)!=0){
                        setMode(0);
                        String end;
                        if (terminate(client.GetChessBoard)!=3){
                            if (client.getPlayer()==terminate(client.GetChessBoard)){
                                end = "Win";
                                result.setText("You Win!");
                            }else{
                                end = "Lost";
                                result.setText("You Lost!");
                            }
                        }else{
                            end = "Draw";
                            result.setText("Game Draw!");
                        }
                        try {
                            MyWrite("PVPGameEnd "+ client.ClientName + " "+ client.OpponentName+ " "+ end +" "+ client.index);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        result1.setText("Game will restart in 5 seconds");
                        client.setPlaying(false);
                        client.setReStart(true);
                        setTURN(false);
                        if (client.getPlayer()==1){
                            setTURN(true);
                        }
                        time = System.currentTimeMillis();
                    }
                }
            }
            if (System.currentTimeMillis() - time > 5000 && client.isReStart()){
                client.setReStart(false);
                client.setPlaying(true);
                Reset();
            }
            if (client.isMiddle()) {
                setMode(0);
                result1.setText("Your opponent is away..");
            }
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        timer.start();
        try {
            this.client = new client();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setUserName(this.client.ClientName);
        pc.setOnMouseClicked(event -> {
            Reset();
            setMode(1);
            try {
                this.client.writer.write("vsPC");
                this.client.writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                client.setPlayer(Integer.parseInt(this.client.reader.readLine()));
                if (client.getPlayer()==2){
                    this.result1.setText("Start game with PC, PC start first");
                    chessBoard = gson.fromJson(this.client.reader.readLine(), int[][].class);
                    drawChess();
                }else{
                    this.result1.setText("Start game with PC, You start first");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        sa.setOnMouseClicked(event -> {
            try {
                SwitchToLogin(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        refresh.setOnMouseClicked(event -> {
            try {
                Refresh();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        check.setOnMouseClicked(event -> {
            Reset();
            if (check.isSelected()){
                button_pvp.setVisible(false);
                refresh.setVisible(false);
                playlist.setVisible(false);
                try {
                    MyWrite("RoomOpen " + getUserName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                result1.setText("Start Searching for player..");
                client.Waiting=true;
            }else {
                button_pvp.setVisible(true);
                refresh.setVisible(true);
                playlist.setVisible(true);
                client.Waiting=false;
                try {
                    MyWrite("RoomClose " + getUserName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                result1.setText("");
            }
        });
        button_pvp.setOnMouseClicked(event -> {
            Reset();
            if (playlist.getValue()==null){
                result1.setText("Please select your opponent");
            }else{
                try {
                    MyWrite("PlayWith "+ playlist.getValue());
                    String[] msg = client.reader.readLine().split("\\s+");
                    if (msg[0].startsWith("PVPStart")){
                        result1.setText("Start Playing with "+ playlist.getValue()+", your opponent start first");
                        client.OpponentName = playlist.getValue();
                        client.index = Integer.parseInt(msg[1]);
                        client.setPlaying(true);
                        setTURN(false);
                        setMode(2);
                        client.setPlayer(2);
                    }
                    else{
                        result1.setText("The selected opponent is not available now..");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        bh.setOnMouseClicked(event -> {
            try {
                SwitchToHistory(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void refreshBoard (int x, int y) throws IOException {
        if (chessBoard[x][y] == EMPTY && mode != 0) {
            BufferedReader reader = this.client.reader;
            switch (getMode()) {
                case 1:
                    chessBoard[x][y] = client.getPlayer();
                    if (terminate(chessBoard) == 0) {
                        Gson gson = new Gson();
                        MyWrite("PCGameStart " + client.getPlayer() + " " + gson.toJson(chessBoard));
                        chessBoard = gson.fromJson(reader.readLine(), int[][].class);
                        int t = terminate(chessBoard);
                        if (t != 0) {
                            setMode(0);
                            if (t == client.getPlayer()) {
                                result.setText("You win!");
                                MyWrite("ResultPC Win");
                            } else if (t == 3) {
                                result.setText("Game draw");
                                MyWrite("ResultPC Draw");
                            } else {
                                result.setText("You lose!");
                                MyWrite("ResultPC Lose");
                            }
                        }
                    } else {
                        setMode(0);
                        int re = terminate(chessBoard);
                        MyWrite(String.valueOf(getUserName()));
                        if (re == 3) {
                            result.setText("Game draw!");
                            MyWrite("ResultPC Draw");
                        } else {
                            if (re == client.getPlayer()) {
                                result.setText("You win!");
                                MyWrite("ResultPC Win");
                            } else {
                                result.setText("Lose");
                                MyWrite("ResultPC Lose");
                            }
                        }
                    }
                    drawChess();
                    break;
                case 2:
                    if (isTURN()){
                        setTURN(false);
                        MyWrite("PVPing " +client.index+" " + client.getPlayer() + " " +x + " "+ y);
                    }
                    break;
            }
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
    public void MyWrite(String content) throws IOException {
        client.writer.write(content+"\n");
        client.writer.flush();
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
        this.client.clientSocket.close();
        Reset();
    }

    public void SwitchToHistory(MouseEvent actionEvent) throws IOException {
        Reset();
        client.setWaiting(false);
        client.setPlaying(false);
        client.setReStart(false);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("History.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Node child = root.getChildrenUnmodifiable().get(4);
        Text text = (Text) child;
        text.setText(client.ClientName);
        text.setVisible(false);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        this.client.clientSocket.close();
    }

    /**
     * Reset.
     */
    public void Reset(){
        setMode(0);
        result.setText("");
        chessBoard = new int[3][3];
        flag = new boolean[3][3];
//        System.out.println(base_square.getChildren());
        base_square.getChildren().remove(5,base_square.getChildren().size());
    }
    public void Refresh() throws IOException {
        MyWrite("UserList");
        ArrayList<String> UserList = gson.fromJson(client.reader.readLine(), ArrayList.class);
        playlist.getItems().remove(0, playlist.getItems().size());
        UserList.forEach(playlist.getItems()::add);
        result1.setText("Player List refreshed..");
    }
    public int[] compare(int[][] chessBoard1, int[][] chessBoard2){
        if (chessBoard1==null || chessBoard2==null){
            return new int[] {-1,-1};
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (chessBoard1[i][j]!=chessBoard2[i][j]){
                    return new int[] {i,j};
                }
            }
        }
        return new int[] {-1,-1};
    }
}
