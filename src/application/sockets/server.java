package application.sockets;


import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class server {
    String term = null;
    int num;
    Hashtable<Socket, BufferedWriter> clientDataHash = new Hashtable<>(50);
    Hashtable<Socket, String> clientNameHash = new Hashtable<>(50);
    Hashtable<String, String> chessPeerHash = new Hashtable<>(50);
    Hashtable<Integer, String> chessBoards = new Hashtable<>(50);
    java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(this.getClass().getName());
    public server(int port) throws IOException {

        //Preparation: Read config
        String configFilePath = "Tic-tac-toe/src/config/config.properties";
        FileInputStream propsInput = new FileInputStream(configFilePath);
        Properties prop = new Properties();
        prop.load(propsInput);
        String DB_User = prop.getProperty("DB_USER");
        String DB_Password = prop.getProperty("DB_PASSWORD");
        String DB_URL = prop.getProperty("DB_URL");

        //Ready to connect database
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager
                    .getConnection(DB_URL,
                            DB_User, DB_Password);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        logger.info("Connect to database");

        try (ServerSocket ss = new ServerSocket(port)){
            logger.info("The socket is running...");
            while (true) {
                Socket socket = ss.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(),
                        StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(),
                        StandardCharsets.UTF_8));
                String affair = reader.readLine();
                switch (affair){
                    case "login":
                        String name_login = reader.readLine();
                        String pwd_login = reader.readLine();
                        boolean login_result = CheckLogin(name_login,pwd_login, conn);
                        MyWrite(writer, String.valueOf(login_result));
                        if (login_result){
                            term = name_login;
                            logger.info(name_login+" login..");
                        }
                        break;
                    case "register":
                        String name_register = reader.readLine();
                        String pwd_register = reader.readLine();
                        boolean register_result = CheckRegister(name_register,pwd_register, conn);
                        MyWrite(writer, String.valueOf(register_result));
                        if (register_result){
                            logger.info(name_register+" register..");
                        }
                        break;
                    case "History":
                        String name_history =reader.readLine();
                        Gson gson = new Gson();
                        MyWrite(writer, gson.toJson(GetHistory(name_history, conn)));
                        break;
                    case "On":
                        MyWrite(writer,term);
                        this.clientDataHash.put(socket, writer);
                        this.clientNameHash.put(socket, term);
                        serverThread serverThread = new serverThread(socket, term,
                                clientDataHash,
                                clientNameHash,
                                chessPeerHash,
                                chessBoards,
                                conn, logger);
                        serverThread.start();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
    public static void main(String[] args) throws IOException {
        server server = new server(7777);
    }
    public static boolean CheckLogin(String UserName, String password, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"Users\" WHERE username = ?");
        stmt.setString(1,UserName);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()){
            return false;
        }
        String real_pwd = rs.getString(3);
        rs.close();
        stmt.close();
        return real_pwd.equals(password);
    }
    public static boolean CheckRegister(String UserName, String pwd, Connection conn) throws SQLException {
        PreparedStatement insert = conn.prepareStatement("INSERT INTO \"Users\" VALUES (Default,?,?)");
        insert.setString(1,UserName);
        insert.setString(2,pwd);
        try {
            insert.execute();
        }catch (SQLException e){
            return false;
        }finally {
            insert.close();
        }
        return true;
    }

    public static ArrayList<String> GetHistory(String UserName, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"Battles\" WHERE \"Player1\" = ? ORDER BY id");
        stmt.setString(1,UserName);
        ResultSet rs = stmt.executeQuery();
        ArrayList<String> result = new ArrayList<>();
        StringBuilder sb;
        while(rs.next()){
            sb = new StringBuilder();
            int id = rs.getInt(1);
            String player = rs.getString(3);
            String res = rs.getString(4);
            sb.append(id).append(" ").append(player).append(" ").append(res);
            result.add(sb.toString());
        }
        rs.close();
        stmt.close();
        System.out.println(result);
        return result;
    }
    private static void MyWrite(BufferedWriter writer, String content) throws IOException {
        writer.write(content+"\n");
        writer.flush();
    }
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
        int num = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (chessBoard[i][j] != 0){
                    num++;
                }
            }
        }
        return num==9? 3:0;
    }
}
