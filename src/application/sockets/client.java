package application.sockets;

import application.controller.Controller;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * The type Client.
 */
public class client {
    /**
     * The Index.
     */
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

    /**
     * The Client socket.
     */
    public Socket clientSocket;
    /**
     * The Client name.
     */
    public String ClientName = null;
    /**
     * The Host.
     */
    String host = "localhost";
    /**
     * The Port.
     */
    int port = 7777;
    /**
     * The Opponent name.
     */
    public String OpponentName = null;
    /**
     * The Waiting.
     */
    public boolean Waiting = false;
    /**
     * The Playing.
     */
    public boolean Playing = false;
    /**
     * The Middle.
     */
    public boolean Middle = false;

    /**
     * Is re start boolean.
     *
     * @return the boolean
     */
    public boolean isReStart() {
        return ReStart;
    }

    /**
     * Sets re start.
     *
     * @param reStart the re start
     */
    public void setReStart(boolean reStart) {
        ReStart = reStart;
    }

    /**
     * The Re start.
     */
    public boolean ReStart = false;

    /**
     * Is middle boolean.
     *
     * @return the boolean
     */
    public boolean isMiddle() {
        return Middle;
    }

    /**
     * Sets middle.
     *
     * @param middle the middle
     */
    public void setMiddle(boolean middle) {
        Middle = middle;
    }

    /**
     * Is waiting boolean.
     *
     * @return the boolean
     */
    public boolean isWaiting() {
        return Waiting;
    }

    /**
     * Sets waiting.
     *
     * @param waiting the waiting
     */
    public void setWaiting(boolean waiting) {
        Waiting = waiting;
    }

    /**
     * Is playing boolean.
     *
     * @return the boolean
     */
    public boolean isPlaying() {
        return Playing;
    }

    /**
     * Sets playing.
     *
     * @param playing the playing
     */
    public void setPlaying(boolean playing) {
        Playing = playing;
    }

    /**
     * Is found boolean.
     *
     * @return the boolean
     */
    public boolean isFound() {
        return Found;
    }

    /**
     * Sets found.
     *
     * @param found the found
     */
    public void setFound(boolean found) {
        Found = found;
    }

    /**
     * The Found.
     */
    public boolean Found = false;
    /**
     * The Get chess board.
     */
    public int[][] GetChessBoard = new int[3][3];
    /**
     * The Writer.
     */
    public BufferedWriter writer;
    /**
     * The Reader.
     */
    public BufferedReader reader;

    /**
     * Instantiates a new Client.
     *
     * @throws IOException the io exception
     */
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
