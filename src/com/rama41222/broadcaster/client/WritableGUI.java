/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.broadcaster.client;

/**
 *
 * @author Rama41222
 */
public interface WritableGUI {
    void write(String s);
    void enableText(boolean b);
    void setOnlineUsers(String [] arr);
    void clearList(boolean b);
    void resetCheckState(boolean b);
    void disableConnect(boolean b);
 
}
