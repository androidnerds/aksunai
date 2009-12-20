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

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.androidnerds.app.aksunai.data.ServerDetail;
import org.androidnerds.app.aksunai.util.AppConstants;
import org.androidnerds.app.aksunai.irc.Server;

public class ConnectionThread implements Runnable {

    private Socket mSock;
    private Server mServer;
    private ServerDetail mServerDetail;
    private BufferedWriter mWriter;
    private BufferedReader mReader;
    private volatile boolean kill = false;
    
    public static final int IRC_PORT = 6667;

    public ConnectionThread(Server s, ServerDetail sd) {
        mServer = s;
        mServerDetail = sd;
    }

    public void disconnect() {
    	requestKill();
    }

    private synchronized void requestKill() {
    	kill = true;
    }
    
    private synchronized boolean shouldKill() {
    	return kill;
    }
    
    public void sendMessage(String message) {
    	try {
            if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "Send message: " + message);
    		mWriter.write(message + "\r\n");
    		mWriter.flush();
    	} catch (IOException e) {
    		
    	}
    }
    
    //TODO: figure out the best way to terminate the process on the user side for the connection exceptions.
    public void run() {
        try {
            if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "Connecting to... " + mServerDetail.mUrl + ":" + mServerDetail.mPort);
            mSock = new Socket(mServerDetail.mUrl, mServerDetail.mPort);
        } catch (UnknownHostException e) {
            if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "UnknownHostException caught, terminating connection process");
        } catch (IOException e) {
            if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught on socket creation. (Server) " + mServer + ", (Exception) " + e.toString());
        }
        
        try {
            mWriter = new BufferedWriter(new OutputStreamWriter(mSock.getOutputStream()));
            mReader = new BufferedReader(new InputStreamReader(mSock.getInputStream()));
        } catch (IOException e) {
            if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught grabbing input/output streams. (Server) " + mServer + ", (Exception) " + e.toString());
        }

        mServer.init(mServerDetail.mPass, mServerDetail.mNick, mServerDetail.mUser, mServerDetail.mRealName);
                
        //watch for server messages.
        try {
            while (!shouldKill()) {
                String message = mReader.readLine();
                if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "Received message: " + message);
            	mServer.receiveMessage(message);
            }
        } catch (IOException e) {
            if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught handling messages. (Server) " + mServer + ", (Exception) " + e.toString());
        }
        
        return;
    }
}
