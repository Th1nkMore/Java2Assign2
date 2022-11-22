package application.sockets;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;

public class clientThread extends Thread{
    Gson gson = new Gson();
    public client client;
    public clientThread(client client) {
        this.client = client;

    }
    public void run()
    {
        while (true)
            {
                try {
                    if (client.isWaiting()) {
                        client.writer.write("Waiting , I'm"+ client.ClientName+"\n");
                        client.writer.flush();
                        String[] msg = client.reader.readLine().split("\\s+");
                        if (msg[0].startsWith("find")){
                            client.setFound(true);
                            client.setPlaying(true);
                            client.index = Integer.parseInt(msg[1]);
                            int len = Integer.parseInt(msg[2]);
                            client.OpponentName = msg[3].substring(0, len);
                            client.setWaiting(false);
                        }
                    }
                    else if (client.isPlaying()){
                        client.writer.write("Playing "+ client.index+"\n");
                        client.writer.flush();
                        ArrayList<String> UserList = gson.fromJson(client.reader.readLine(), ArrayList.class);
                        client.GetChessBoard = gson.fromJson(client.reader.readLine(), int[][].class);
                        if (UserList!= null && !UserList.contains(client.OpponentName)){
                            client.setPlaying(false);
                            client.setMiddle(true);
                        }
                    }
                    else {
                        client.writer.write("Hello , I'm"+ client.ClientName+"\n");
                        client.writer.flush();
                    }


                }catch (IOException e){
                    System.err.println("Client Thread Failed to send heartbeat message!");
                    try {
                        client.clientSocket.close();
                        System.err.println("Socket Closed");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                    } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            }
    }
