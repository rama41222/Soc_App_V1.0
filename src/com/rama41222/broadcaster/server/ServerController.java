/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.broadcaster.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This Class Handles the tasks for the server Acts as a mediator for all
 * clients Server is singleton. Only a one instance of the server will created.
 *
 * @version SoC Chat v1.0
 * @author Rama41222
 */
public class ServerController {

    //Hashmap stores username and the printwriter for a particular user
    private static final Map<String, PrintWriter> onlineUserList = new HashMap<>();
    private static int PORT;
    private static WritableGUI gui;
    //Eager instantiation of the server
    private static final ServerController server = new ServerController();

    // private Lifecycle method 
    private ServerController() {
    }

    //Returns the same instance everytime it's asked
    public static ServerController getInstance() {
        return server;
    }

    public void setParameters(int serverPort, WritableGUI g) {
        PORT = serverPort;
        gui = g;
    }

    //Starts the run method of the client hanlder private class
    public void startThread() throws IOException {
        gui.write("The chat server is running at port " + PORT);
        ServerSocket listener = new ServerSocket(PORT); //Adds a socket listenr to ther server port
            while (true) {
                new ClientHandler(listener.accept()).start(); //starts a new instance of  clientHandler therad when a new client is connected
            }
        
    }

    private static class ClientHandler extends Thread {

        private String name;
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        //Used to generate and send the online user list
        private void sendOnlineUsers() {

            Set mapkeys = onlineUserList.keySet();
            String userlist = "";
            for (Object key : mapkeys) {
                userlist = userlist + key.toString() + ",";
            }
            out.println("Sending_userlist");
            gui.write("Sending the userlist to: " + name);
            out.println(userlist);
        }

        @Override
        public void run() {
            try {
                //Setting up the input and output streams
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                //submit name
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();

                    if (name == null) {
                        return;
                    }
                    synchronized (onlineUserList) {
                        if (!onlineUserList.containsKey(name)) {
                            onlineUserList.put(name, out);
                            gui.write("Connected to the host: " + name);
                            out.println("Connection_Accepted");

                            this.sendOnlineUsers();
                            break;
                        } else {
                            gui.write("Invalid Name request from host: " + name);
                            out.println("Invalid_Name");
                            break;
                        }
                    }
                }
                //private chat handler (unicast messaging)
                while (true) {
                    String input = in.readLine();

                    if (input == null) {
                        break;
                    }
                    if ("private_message".equals(input)) {
                        
                        String myName = in.readLine();
                        String userList = in.readLine();

                        userList = userList.replaceFirst(",$", "");
                        String userListArr[] = userList.split("\\s*,\\s*");

                        String message = in.readLine();
                        if ("bye".equals(message)) {
                            gui.write("Connection terminated with: " + myName);
                            out.println("quit_private_chat");
                            continue;
                        }
                        out.println("MESSAGE " + this.name + " : " + message);
                        for (String userName : userListArr) {

                            //outputs the message into a specific number of users send from client
                            synchronized (onlineUserList) {
                                if (onlineUserList.containsKey(userName)) {
                                    PrintWriter value = onlineUserList.get(userName);
                                    value.println("MESSAGE " + myName + " : " + message);
                                    sendOnlineUsers();
                                }
                            }
                        }
                        continue;
                    }

                    //Broadcast function
                    Set mapkeys = onlineUserList.keySet();
                    String userlist = "";
                    for (Object key : mapkeys) {
                        
                        userlist = userlist + key.toString() + ",";
                    }
                    Iterator iterator = mapkeys.iterator();
                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        PrintWriter value = onlineUserList.get(key);
                        value.println("MESSAGE " + this.name + " : " + input);
                        value.println("Sending_userlist");
                        value.println(userlist);
                    }
                }
            } catch (Exception e) {
                gui.write(e.toString());
            } finally {
                if (name != null) {
                    gui.write("Connection terminated with: " + name);
                    onlineUserList.remove(name);
                    this.sendOnlineUsers();
                }
                try {
                    socket.close();
                } catch (Exception e) {
                    gui.write(e.toString());
                }
            }
        }
    }
}
