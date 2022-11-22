package application.sockets;

import application.controller.Controller;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class client {
    public int index;
    private int player = 0;

    /**
     * Gets player.
     *
     * @return the player
     */
    public int getPlayer() {
        return player;
    }

    /**
     * Sets player.
     *
     * @param player the player
     */
    public void setPlayer(int player) {
        this.player = player;
    }
    public Socket clientSocket;
    public String ClientName = null;
    String host = "localhost";
    int port = 7777;
    public String OpponentName = null;
    public boolean Waiting = false;
    public boolean Playing = false;
    public boolean Middle = false;

    public boolean isReStart() {
        return ReStart;
    }

    public void setReStart(boolean reStart) {
        ReStart = reStart;
    }

    public boolean ReStart = false;

    public boolean isMiddle() {
        return Middle;
    }

    public void setMiddle(boolean middle) {
        Middle = middle;
    }

    public boolean isWaiting() {
        return Waiting;
    }

    public void setWaiting(boolean waiting) {
        Waiting = waiting;
    }

    public boolean isPlaying() {
        return Playing;
    }

    public void setPlaying(boolean playing) {
        Playing = playing;
    }

    public boolean isFound() {
        return Found;
    }

    public void setFound(boolean found) {
        Found = found;
    }

    public boolean Found = false;
    public int[][] GetChessBoard = new int[3][3];
    public BufferedWriter writer;
    public BufferedReader reader;
    public client() throws IOException {
        this.clientSocket = new Socket(host, port);
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
         BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
         this.writer = writer;
         this.reader = reader;
         writer.write("On\n");
         writer.flush();
         this.ClientName = reader.readLine();
         clientThread clientThread = new clientThread(this);
         clientThread.start();
         System.out.println("Client Thread start");
    }
}
