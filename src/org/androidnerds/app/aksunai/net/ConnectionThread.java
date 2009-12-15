/*
 * Copyright (C) 2009  AndroidNerds.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.androidnerds.app.aksunai.net;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.androidnerds.app.aksunai.MyConfig;

public class ConnectionThread implements Runnable {

    private Socket mSock;
    private String mURL;
    private Server mServer;
    private String mNick;
    private String mUser;
    private String mPassword;
    private String mRealName;
    private int mPort;
    private volatile boolean kill = false;
    
    public static final int IRC_PORT = 6667;

    public ConnectionThread(String url, String nick, String user, String password, int port, String realName, Server server) {
        mURL = url;
        mNick = nick;
        mUser = user;
        mServer = server;
        mPassword = password;

        if (MyConfig.DEBUG) Log.d("Aksunai", "Creating connection thread to " + mURL + ":" + port);

        if (realName == null || realName.equals("")) {
            mRealName = "Aksunai Android Client";
        } else {
            mRealName = realName;
        }

        mPort = port;
        if (mPort == 0) {
            mPort = IRC_PORT;
        }
    }

    public void disconnect() {
        try {
            mSock.close();
            mServer.state = Server.STATE_DISCONNECTED;
            requestKill();
        } catch (IOException e) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "IOException caught on ConnectionThread disconnect: (Server) " + mServer + ", (Exception) " + e.toString());
        }
    }

    private synchronized void requestKill() {
    	kill = true;
    }
    
    private synchronized boolean shouldKill() {
    	return kill;
    }
    
    //TODO: figure out the best way to terminate the process on the user side for the connection exceptions.
    public void run() {
        try {
            if (MyConfig.DEBUG) Log.d("Aksunai", "Connecting to... " + mURL + ":" + mPort);
            mSock = new Socket(mURL, mPort);
        } catch (UnknownHostException e) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "UnknownHostException caught, terminating connection process");
        } catch (IOException e) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "IOException caught on socket creation. (Server) " + mServer + ", (Exception) " + e.toString());
        }

        mServer.state = Server.STATE_CONNECTING;

        try {
            mServer.writer = new BufferedWriter(new OutputStreamWriter(mSock.getOutputStream()));
            mServer.reader = new BufferedReader(new InputStreamReader(mSock.getInputStream()));
        } catch (IOException e) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "IOException caught grabbing input/output streams. (Server) " + mServer + ", (Exception) " + e.toString());
        }

        try {
            mServer.writer.write("NICK " + mNick + "\r\n");
            mServer.writer.write("USER " + mUser + " 8 * :" + mRealName + "\r\n");
            mServer.writer.flush();
        } catch (IOException e) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "IOException caught sending login information. (Server) " + mServer + ", (Exception) " + e.toString());
        }

        String line = null;

        try {
            while ((line = mServer.reader.readLine()) != null) {
            	
                if (MyConfig.DEBUG) Log.d("Aksunai", "Server Message: " + line);

                if (line.indexOf("001") >= 0) {
                    mServer.state = Server.STATE_CONNECTED;

                    if (mServer.connectionState == Server.RECONNECTING) {
                        mServer.loadState();
                    }

                    Message.obtain(mServer.mHandler, Server.STATE_CONNECTED, "connected").sendToTarget();
                } else if (line.indexOf("004") >= 0) {
                    break;
                } else if (line.indexOf("433") >= 0) {
                    //mServer.state = Server.STATE_NICK_IN_USE;
                    if (MyConfig.DEBUG) Log.d("Aksunai", "NICK_IN_USE raised.");

                    mServer.writer.write("NICK " + mNick + "_\r\n");
                    mServer.writer.write("USER " + mUser + " 8 * :" + mRealName + "\r\n");
                    mServer.writer.flush();

                    //notify the ui and prompt to change.
                    if (mServer.mHandler != null) {
                        Message.obtain(mServer.mHandler, Server.STATE_NICK_IN_USE, "connect").sendToTarget();
                    }
                } else if (line.indexOf("432") >= 0) {
                    //mServer.state = Server.STATE_NICK_BAD;

                    if (MyConfig.DEBUG) Log.d("Aksunai", "Critical Error: STATE_NICK_BAD");

                    //there is a system error with the user's nick. notify them.
                    if (mServer.mHandler != null) {
                        Message.obtain(mServer.mHandler, Server.STATE_NICK_BAD, "connect").sendToTarget();
                    }

                    return;
                } else if (line.startsWith("ERROR")) {
                    if (line.endsWith("(Connection Timed Out)")) {
                        //the server ended the connection.
                    }
                } else if (line.startsWith("PING")) {
                    mServer.writer.write("PONG " + line.substring(5) + "\r\n");
                    mServer.writer.flush();
                }
            }
        } catch (IOException e) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "IOException caught receiving login feedback. (Server) " + mServer + ", (Exception) " + e.toString());
        }

        //TODO: add in feature for autojoining channels.
        try {
            if (!mPassword.equals("")) {
                mServer.writer.write("PRIVMSG NickServ :identify " + mPassword + "\r\n");
                mServer.writer.flush();
            }

            //mServer.state = Server.STATE_READY;
        } catch (IOException e) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "IOException caught identifying nick. (Server) " + mServer + ", (Exception) " + e.toString());
        }

        if (MyConfig.DEBUG) Log.d("Aksunai", "Preparing to listen for server messages.");

        //watch for server messages.
        try {
            while (!shouldKill()) {
            	line = mServer.reader.readLine();
            	
                if (line.startsWith("PING")) {
                    mServer.writer.write("PONG " + line.substring(5) + "\r\n");
                    mServer.writer.flush();
                } else {
                    mServer.getLine(line);
                }
            }
        } catch (IOException e) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "IOException caught handling messages. (Server) " + mServer + ", (Exception) " + e.toString());
        }
        
        return;
    }
}
