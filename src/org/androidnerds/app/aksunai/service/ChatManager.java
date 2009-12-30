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
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Collections;
import java.util.Map;

import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.data.ServerDetail;
import org.androidnerds.app.aksunai.irc.Message;
import org.androidnerds.app.aksunai.irc.Server;
import org.androidnerds.app.aksunai.irc.MessageList;
import org.androidnerds.app.aksunai.irc.MessageList.NewMessageListener;
import org.androidnerds.app.aksunai.irc.Server.MessageListListener;
import org.androidnerds.app.aksunai.net.ConnectionManager;
import org.androidnerds.app.aksunai.preferences.PreferenceConstants;
import org.androidnerds.app.aksunai.ui.ChatActivity;
import org.androidnerds.app.aksunai.util.AppConstants;
import org.androidnerds.app.aksunai.util.LowerHashMap;

/**
 * 
 * @author mike@androidnerds.org
 * @version 1
 * 
 * The ChatManager service is responsible for communicating the irc messages from the thread to the UI.
 */
public class ChatManager extends Service implements OnSharedPreferenceChangeListener, MessageListListener, NewMessageListener {

    private final IBinder mBinder = new ChatBinder();
    private NotificationManager mNotificationManager;
    private ConnectionManager mConnectionManager = new ConnectionManager(this);
    private ChatActivity mChatActivity;
    protected SharedPreferences mPrefs;
    public Map<String, Server> mConnections = Collections.synchronizedMap(new LowerHashMap<Server>());
    public static boolean running = false;
	
    @Override
    public void onCreate() {
    	Log.i(AppConstants.CHAT_TAG, "Creating the chat service.");
		
    	mPrefs = getSharedPreferences("aksunai-prefs", MODE_PRIVATE);
    	mPrefs.registerOnSharedPreferenceChangeListener(this);
		
    	mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
        //testing out this setting. it might not be needed.
        setForeground(true);
        running = true;
    }

    @Override
    public void onDestroy() {
       	Log.d(AppConstants.CHAT_TAG, "Service is being destroyed");
    }

	@Override
	public void onStart(Intent intent, int startId) {
		
	}
	
    protected void stop() {
        if (mConnections.isEmpty()) {
            running = false;
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
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        
        Log.d(AppConstants.CHAT_TAG, "Something rebound to the ChatManager");
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
        
        Server s = mConnections.get(serverName);
        MessageList ml = s.mMessageLists.get(messageListName);
        ml.setOnNewMessageListener(this);
		boolean notify = mPrefs.getBoolean("pref_notification_bar", false);
		
        if (notify && ml.mType == MessageList.Type.PRIVATE) {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("server", serverName);
            i.putExtra("chat", messageListName);
            
            PendingIntent pending = PendingIntent.getActivity(this, 0, i, 0);
            Notification n = new Notification(android.R.drawable.stat_notify_chat, "New Message From " + messageListName, System.currentTimeMillis());
            n.setLatestEventInfo(this, "Aksunai", "New Message From " + messageListName, pending);

			String ringtone = mPrefs.getString("notification-ringtone", "");
			
			if (ringtone.equals("")) {
				n.defaults |= Notification.DEFAULT_SOUND;
			} else {
				n.sound = Uri.parse(ringtone);
			}
			
			if (mPrefs.getBoolean("pref_notification_vibrate", false)) {
				n.vibrate = new long[] { 100, 250, 100, 250 };
			}
			
            mNotificationManager.notify(R.string.notify_new_private_chat, n);
        }
	}
	
	public void onCloseMessageList(String serverName, String messageListName) {
        if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "onCloseMessageList(" + serverName + ", " + messageListName + ")");
        // TODO: drop the ChatView and remove it from the NewMessage listeners
    }

    public void onNewMessage(String message, String server, String list) {
		//look for the nick in this message, if so we need to set a notification.
		Server s = mConnections.get(server);
		boolean notify = mPrefs.getBoolean("pref_notification_bar", false);
				
		if (notify && message.contains(s.mNick)) {
			if (!Character.isLetterOrDigit(message.charAt(message.indexOf(s.mNick) + s.mNick.length() - 1))) {				
				Intent i = new Intent(this, ChatActivity.class);
	            i.putExtra("server", server);
	            i.putExtra("chat", list);

	            PendingIntent pending = PendingIntent.getActivity(this, 0, i, 0);
	            Notification n = new Notification(android.R.drawable.stat_notify_chat, "New Message In " + list, System.currentTimeMillis());
	            n.setLatestEventInfo(this, "Aksunai", "New Message In " + list, pending);
	
				String ringtone = mPrefs.getString("notification-ringtone", "");
				
				if (ringtone.equals("")) {
					n.defaults |= Notification.DEFAULT_SOUND;
				} else {
					n.sound = Uri.parse(ringtone);
				}
				
				if (mPrefs.getBoolean("pref_notification_vibrate", false)) {
					n.vibrate = new long[] { 100, 250, 100, 250 };
				}
				
	            mNotificationManager.notify(R.string.notify_nick_in_chat, n);
			}
		}
    }
}
