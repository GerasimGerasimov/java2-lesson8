/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.jchat.core.server;

/**
 *
 * @author info
 */
public class ConnectionState {
    private String nick;
    private boolean onLine; 
    
    ConnectionState(String _nick, boolean _onLine) {
        this.nick = _nick;
        this.onLine = _onLine;    
    }
    
    public boolean isOnline() {
        return this.onLine;
    }
    
    public void getOnline (boolean _onLine) {
        this.onLine = _onLine;
    }
    
}
