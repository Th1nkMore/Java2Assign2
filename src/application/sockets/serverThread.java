package application.sockets;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.stream.Collectors;

public class serverThread extends Thread{
    Gson gson = new Gson();
    Random random = new Random();
    boolean SetOpponent = false;
    String ClientName;
    Socket clientSocket;
    final Hashtable<Socket, BufferedWriter> clientDataHash;
    final Hashtable<Socket, String> clientNameHash;
    final Hashtable<String, String> chessPeerHash;
    final Hashtable<Integer, String> chessBoards;
    boolean isClientClosed = false;
    boolean TurnChange = false;
    String msg;
    public Connection conn = null;
    public java.util.logging.Logger logger;
    long startTime;
    public serverThread(Socket clientSocket,
                        String clientName,
                        Hashtable<Socket, BufferedWriter> clientDataHash,
                        Hashtable<Socket, String> clientNameHash,
                        Hashtable<String, String> chessPeerHash,
                        Hashtable<Integer, String> chessBoards,
                        Connection conn,
                        java.util.logging.Logger logger){
        this.clientSocket = clientSocket;
        this.clientDataHash = clientDataHash;
        this.clientNameHash = clientNameHash;
        this.chessPeerHash = chessPeerHash;
        this.chessBoards = chessBoards;
        this.conn = conn;
        this.startTime =  System.currentTimeMillis();
        this.logger = logger;
        this.ClientName = clientName;
    }

    public void Feedback(String received) throws SQLException {
        if (received.startsWith("Hello")){
            this.startTime =  System.currentTimeMillis();
        }
        if (received.startsWith("Waiting") && !SetOpponent){
            this.startTime =  System.currentTimeMillis();
            reply("wait");
        }
        if (received.startsWith("Playing")){
            String[] msg = received.split("\\s+");
            this.startTime =  System.currentTimeMillis();
            int index = Integer.parseInt(msg[1]);
            synchronized (clientNameHash){
                reply(gson.toJson(new ArrayList<>(clientNameHash.values())));
            }
            synchronized (chessBoards){
                reply(chessBoards.get(index));
            }
        }
        if (received.startsWith("vsPC")) {
            int[][] chessboard_PC = new int[3][3];
            int PC = random.nextDouble() > 0.5 ? 1 : 2;
            int player = -1 * PC + 3;
            reply(String.valueOf(player));
            logger.info(ClientName+" starts a game with PC");
            if (PC == 1) {
                chessboard_PC[random.nextInt(2)][random.nextInt(2)] = PC;
                reply(gson.toJson(chessboard_PC));
            }
        }
        if (received.startsWith("PCGame")) {
            String[] msg = received.split("\\s+");
            ArrayList<Integer> list = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                list.add(i);
            }
            int pid = Integer.parseInt(msg[1]);
            int[][] next = gson.fromJson(msg[2], int[][].class);
            int get = list.get(random.nextInt(list.size()));
            list.remove((Integer) get);
            while (next[get / 3][get % 3] != 0) {
                get = list.get(random.nextInt(list.size()));
                list.remove((Integer) get);
            }
            next[get / 3][get % 3] = 3 - pid;
            reply(gson.toJson(next));
        }
        if (received.startsWith("ResultPC")) {
            String[] msg = received.split("\\s+");
            PCGameEnd(ClientName, msg[1], this.conn);
        }
        if (received.startsWith("UserList")){
            ArrayList<String> UserList =
                    (ArrayList<String>) chessPeerHash.keySet().stream()
                            .filter(e -> chessPeerHash.get(e).equals("wait"))
                            .collect(Collectors.toList());
            reply(gson.toJson(UserList));
        }
        if (received.startsWith("RoomOpen")){
            String[] msg = received.split("\\s+");
            this.chessPeerHash.put(msg[1],"wait");
        }
        if (received.startsWith("RoomClose")){
            String[] msg = received.split("\\s+");
            this.chessPeerHash.remove(msg[1],"wait");
        }
        if (received.startsWith("PlayWith")){
            String[] msg = received.split("\\s+");
            boolean still_alive;
            synchronized (chessPeerHash){
                still_alive = chessPeerHash.containsKey(msg[1]);
                logger.info(ClientName+ " wants to play with "+ msg[1]);
            }
            if (still_alive){
                int num = random.nextInt(100000);
                reply("PVPStart "+ num);
                reply_to("find "+ num + " " + ClientName.length() +" "+ ClientName, msg[1]);
                SetOpponent = true;
                synchronized (chessBoards){
                    chessBoards.put(num, gson.toJson(new int[3][3]));
                }
                synchronized (chessPeerHash){
                    if (chessPeerHash.containsKey(msg[1]) && chessPeerHash.get(msg[1]).equals("wait")){
                        chessPeerHash.put(msg[1], ClientName);
                    }
                }
            }else{
                reply("opponent leave");
            }
        }
        if (received.startsWith("PVPing")){
            String[] msg = received.split("\\s+");
            int index = Integer.parseInt(msg[1]);
            int player = Integer.parseInt(msg[2]);
            int x = Integer.parseInt(msg[3]);
            int y = Integer.parseInt(msg[4]);
            synchronized (chessBoards){
                int[][] board = gson.fromJson(chessBoards.get(index), int[][].class);
                if (board[x][y]==0){
                    board[x][y] = player;
                }
                chessBoards.put(index, gson.toJson(board));
            }
        }
        if (received.startsWith("PVPGameEnd")){
            String[] msg = received.split("\\s+");
            PVPGameEnd(msg[1],msg[2],msg[3], this.conn);
            int index = Integer.parseInt(msg[4]);
            chessBoards.put(index, gson.toJson(new int[3][3]));
        }
    }
    public void reply_to(String msg, String username){
        Socket target;
        BufferedWriter writer;
        synchronized (clientNameHash){
            target = (Socket) getHashKey(clientNameHash, username);
        }
        synchronized (clientDataHash){
            writer = clientDataHash.get(target);
        }
        try {
            writer.write(msg);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Object getHashKey(Hashtable targetHash, Object hashValue){
        Object hashKey;
        for (Enumeration enu = targetHash.keys(); enu.hasMoreElements();){
            hashKey = enu.nextElement();
            if (hashValue.equals(targetHash.get(hashKey)))
                return hashKey;
        }
        return null;
    }
    public void reply(String msg){
        synchronized (clientDataHash){
            BufferedWriter writer = clientDataHash.get(clientSocket);
            try {
                writer.write(msg+"\n");
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void closeClient(){
        synchronized (chessPeerHash)
        {
            chessPeerHash.remove(clientNameHash.get(clientSocket));
            if (chessPeerHash.containsValue(clientNameHash.get(clientSocket)))
            {
                chessPeerHash.put((String) getHashKey(chessPeerHash,
                                clientNameHash.get(clientSocket)),
                        "closed");
            }
        }
        synchronized (clientDataHash)
        {
            clientDataHash.remove(clientSocket);
        }
        synchronized (clientNameHash)
        {
            clientNameHash.remove(clientSocket);
        }
//        sendPublicMsg(gson.toJson(chessPeerHash.keySet()));
        try
        {
            clientSocket.close();
        }
        catch (IOException exx)
        {
            exx.printStackTrace();
        }
        isClientClosed = true;
    }

    private void sendPublicMsg(String publicMsg) {
        synchronized (clientDataHash)
        {
            for (Enumeration<BufferedWriter> enu = clientDataHash.elements(); enu
                    .hasMoreElements();)
            {
                BufferedWriter writer = enu.nextElement();
                try
                {
                    writer.write(publicMsg);
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public static boolean PCGameEnd(String UserName, String result, Connection conn) throws SQLException {
        PreparedStatement insert = conn.prepareStatement("INSERT INTO \"Battles\" VALUES (Default,?,'PC',?)");
        insert.setString(1, UserName);
        insert.setString(2, result);
        try{
            insert.execute();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
    public static boolean PVPGameEnd(String UserName1,String UserName2, String result, Connection conn) throws SQLException {
        PreparedStatement insert = conn.prepareStatement("INSERT INTO \"Battles\" VALUES (Default,?,?,?)");
        insert.setString(1, UserName1);
        insert.setString(2, UserName2);
        insert.setString(3, result);
        try{
            insert.execute();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
    public void run()
    {
        BufferedReader reader;
        try
        {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true)
            {
                if (System.currentTimeMillis() - this.startTime > 1000){
                    this.isClientClosed = true;
                    logger.info(clientNameHash.get(clientSocket)+" is away..");
                    closeClient();
                    this.clientSocket.close();
                    break;
                }
                try {
                    String received = reader.readLine();
                    Feedback(received);
                }catch (Exception e){
                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException ignore){}
                }
            }
        }
        catch (IOException ignored){}
//        finally
//        {
//            if (isClientClosed)
//            {
//                closeClient();
//            }
//        }
    }
}
