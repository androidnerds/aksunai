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
package org.androidnerds.app.aksunai.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.androidnerds.app.aksunai.data.ServerDetail;
import org.androidnerds.app.aksunai.irc.Message;
import org.androidnerds.app.aksunai.irc.Server;
import org.androidnerds.app.aksunai.irc.MessageList;
import org.androidnerds.app.aksunai.irc.Server.MessageListListener;
import org.androidnerds.app.aksunai.net.ConnectionManager;
import org.androidnerds.app.aksunai.preferences.PreferenceConstants;
import org.androidnerds.app.aksunai.ui.ChatActivity;
import org.androidnerds.app.aksunai.util.AppConstants;

/**
 * 
 * @author mike@androidnerds.org
 * @version 1
 * 
 * The ChatManager service is responsible for communicating the irc messages from the thread to the UI.
 */
public class ChatManager extends Service implements OnSharedPreferenceChangeListener, MessageListListener {

    private final IBinder mBinder = new ChatBinder();
    private NotificationManager mNotificationManager;
    private ConnectionManager mConnectionManager;
    private ChatActivity mChatActivity;
    protected SharedPreferences mPrefs;
    public Map<String, Server> mConnections;
	
    @Override
    public void onCreate() {
    	Log.i(AppConstants.CHAT_TAG, "Creating the chat service.");
		
    	mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	mPrefs.registerOnSharedPreferenceChangeListener(this);
		
    	mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mConnectionManager = new ConnectionManager(this);
		mConnections = Collections.synchronizedMap(new HashMap<String, Server>());
		
        //testing out this setting. it might not be needed.
        setForeground(true);
    }

    @Override
    public void onDestroy() {
       	
    }

    protected void stop() {
        if (mConnections.isEmpty()) {
        	stopSelf();

        }
    }
	
    public void openServerConnection(ChatActivity chatActivity, ServerDetail details) {
        this.mChatActivity = chatActivity;
        Server server = mConnectionManager.openConnection(details);
        server.setMessageListListener(this);
        if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Adding new Server to the connections: " + server);
        mConnections.put(server.mName, server);
    }
	
    /**
     * This method sends a notification to the user if they have the preference set.
     */
    public void sendNotification() {
        if (!mPrefs.getBoolean(PreferenceConstants.NOTIFICATIONS, false)) {
        	return;
        }	
    }
	
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(AppConstants.CHAT_TAG, "Something is bound to the ChatManager");
		
        //binded services don't stay running. let's make sure we do.
        startService(new Intent(this, ChatManager.class));
		
        return mBinder;
    }
	
    @Override
    public boolean onUnbind(Intent intent) {
       	
        if (mConnections.isEmpty()) {
        	stop();
        }
		
        return true;
    }
	
    public class ChatBinder extends Binder {
        public ChatManager getService() {
        	return ChatManager.this;
        }
    }
	
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        //we need to keep an eye on the preferences while connected.
        mPrefs = preferences;
    }
	
	/**
	 * MessageListeners
	 */
	public void onNewMessageList(String serverName, String messageListName) {
        if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "onNewMessageList(" + serverName + ", " + messageListName + ")");
	    mChatActivity.createChat(serverName, messageListName);	
        // TODO: notify if it's a PM (or notice?) and bring to front if it's a channel
	}
	
	public void onCloseMessageList(String serverName, String messageListName) {
        if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "onLeave(" + serverName + ", " + messageListName + ")");
        // TODO: drop the ChatView and remove it from the NewMessage listeners
    }
}
