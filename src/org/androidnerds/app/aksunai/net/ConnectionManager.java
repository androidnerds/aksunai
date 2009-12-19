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

import android.content.Context;
import android.util.Log;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.androidnerds.app.aksunai.data.ServerDetail;
import org.androidnerds.app.aksunai.irc.MessageList;
import org.androidnerds.app.aksunai.irc.Server;
import org.androidnerds.app.aksunai.util.AppConstants;

/**
 * ConnectionManager handles the network connections to the servers. It also holds a list of
 * {@link org.androidnerds.app.aksunai.irc.Server} to which the messages from the servers will
 * be sent, and from which the messages to the servers will be received.
 */
public class ConnectionManager {
    private Map<Server, ConnectionThread> mConnections;
    public Context mContext;
    
    public ConnectionManager(Context c) {
    	mContext = c;
    	mConnections = Collections.synchronizedMap(new HashMap<Server, ConnectionThread>());
    }
    
    /**
     * takes a formatted string and writes it to the connection to the server wich corresponds to the
     * {@link org.androidnerds.app.aksunai.irc.Server}
     */
    public void sendMessage(Server server, String message) {
    }
    
    public Server openConnection(ServerDetail sd) {
    	Server s = new Server(this, sd.mName);
    	
    	ConnectionThread t = new ConnectionThread(s, sd);
    	Thread thr = new Thread(t);
    	thr.start();
    	
    	mConnections.put(s, t);
    	
    	return s;
    }
}

