/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.broadcaster.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This Class Handles the :message transmission to the connected server.
 * Acts as a message listener for the incoming messages from the server
 * 
 * @version SoC Chat v1.0
 * @author Rama41222
 */
public class ClientManager {
    //Declaring a variable of Writable GUI type
    WritableGUI gui;
    private Socket sock = null;

    private BufferedReader in;
    private PrintWriter out;

    private String serverAddress;
    private String clientName;
    private int serverPort;

    public ClientManager() {}
    
    /** Set the client manager details
     * @param gui
     * @param serverAddress
     * @param serverPort
     * @param clientName
     */
    public void setClientDetails(WritableGUI gui, String serverAddress, int serverPort, String clientName) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.clientName = clientName;
        this.gui = gui;
    }

    private String getClientName() {
        return this.clientName;
    }
    //Sends a public message to the server
    public void sendMessage(String message) {
        out.println(message);
    }
    
    //Sends a private message to the server
    public void sendPrivateMessage(String userList,String sender) {
        
        out.println("private_message");
        out.println(sender);
        out.println(userList);
    }

    /**
     * Creates a new socket
     * Sets the input stream from server through the socket.
     * Sets the output streams to the server from the socket.
     * When the connection is established, the client will respond to the messages coming back and forth from server accordingly.
     * All the protocols are defined inside the while loop
     */
    void connectionEstablish() {
 
        try {
            //Opens a socket 
            sock = new Socket(this.serverAddress, this.serverPort);
            //Sets the input stram from server and output stream from client
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(sock.getOutputStream(), true);
            String line = "";
            
            while ((line = in.readLine()) != null) {
                //Submitting username to server
                if (line.startsWith("SUBMITNAME")) {
              
                    out.println(this.getClientName());
                 
                } else if (line.startsWith("Invalid_Name")) { //if the name already exists  
                    
                    gui.write("Your Username already exists. Please Submit a new username");
                    
                } else if (line.startsWith("Connection_Accepted")) { //When Connection accepted
                    
                    gui.disableConnect(false);
                    gui.write("Connection Successful!");
                    gui.enableText(true);
                    
                } else if (line.startsWith("MESSAGE")) { // When a new message comes
                    
                    line = line.replaceFirst("^MESSAGE", "");// Removes the 'MESSAGE' from message
                    gui.write(line);
                    
                } else if (line.startsWith("Sending_userlist")) { // Sending the user list
                    
                    String ulist = in.readLine();
                    
                    if (ulist != null || "".equals(ulist)) {
                        //formatting user list
                        ulist = ulist.replaceFirst(",$", "");
                        String[] userArray = ulist.split("\\s*,\\s*");
                        gui.setOnlineUsers(userArray);    //setting the userlist on online users list box via WritableGUI interface
                    }
                } else if (line.startsWith("quit_private_chat")) { //Quitting from private chat
                    
                    gui.clearList(true);
                    gui.resetCheckState(false);
                } 
            }
        } catch (UnknownHostException e) {
            gui.write("Unknown Host");
            System.exit(1);
        } catch (IOException ie) {
            gui.write("Connection refused to the host");
            System.exit(1);
        }
    }
}
