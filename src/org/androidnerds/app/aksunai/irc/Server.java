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
package org.androidnerds.app.aksunai.irc;

import android.util.Log;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.androidnerds.app.aksunai.net.ConnectionManager;
import org.androidnerds.app.aksunai.util.AppConstants;

/**
 * Server is the holder for everything related to the server, its messages, notices, channels, private messages,
 * nick name, username, real name...
 * <p>
 * It has three input/output points:
 * <ul>
 *     <li>receiveMessage: takes a raw string from the ConnectionManager</li>
 *     <li>sendMessage: sends a raw string to the ConnectionManager</li>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server.MessageListener}: listeners may register with {@link org.androidnerds.app.aksunai.irc.Server#setOnNewMessageListener}</li>
 * </ul>
 */
public class Server extends MessageList {
    private ConnectionManager mConnectionManager;
    public String mNick;
    private List<MessageListener> mListeners;
    public Map<String, Channel> mChannels;
    public Map<String, Private> mPrivates;
    public Map<String, Notice> mNotices;

    /**
     * Class constructor.
     *
     * @param title a String, used as window title by the ChatManager
     */
    public Server(ConnectionManager cm, String title) {
        super(title);
        this.mConnectionManager = cm;
        this.mListeners = Collections.synchronizedList(new ArrayList<MessageListener>());
        this.mChannels = Collections.synchronizedMap(new HashMap<String, Channel>());
        this.mPrivates = Collections.synchronizedMap(new HashMap<String, Private>());
        this.mNotices = Collections.synchronizedMap(new HashMap<String, Notice>());
    }

    /**
     * Message Listener. Listeners must implement all four of the following methods:
     * <ul>
     *     <li>public void onNewServerMessage(Message message, Server messageList);</li>
     *     <li>public void onNewChannelMessage(Message message, Channel messageList);</li>
     *     <li>public void onNewPrivateMessage(Message message, Private messageList);</li>
     *     <li>public void onNewNoticeMessage(Message message, Notice messageList);</li>
     * </ul>
     */
    public interface MessageListener {
        public void onNewServerMessage(Message message, Server messageList);
        public void onNewChannelMessage(Message message, Channel messageList);
        public void onNewPrivateMessage(Message message, Private messageList);
        public void onNewNoticeMessage(Message message, Notice messageList);
    }

    /**
     * registers as a message listener.
     */
    public void setOnNewMessageListener(MessageListener ml) {
        mListeners.add(ml);
    }

    /**
     * notifies the listeners that a new server message is available.
     *
     * @param message the new {@link org.androidnerds.app.aksunai.irc.Message}
     * @param messageList the {@link org.androidnerds.app.aksunai.irc.Server} holding this new message
     */
    public void notifyNewServerMessage(Message message, Server messageList) {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about new server message: " + message);
            ml.onNewServerMessage(message, messageList);
        }
    }

    /**
     * notifies the listeners that a new channel message is available.
     *
     * @param message the new {@link org.androidnerds.app.aksunai.irc.Message}
     * @param messageList the {@link org.androidnerds.app.aksunai.irc.Channel} holding this new message
     */
    public void notifyNewChannelMessage(Message message, Channel messageList) {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about new channel message: " + message);
            ml.onNewChannelMessage(message, messageList);
        }
    }

    /**
     * notifies the listeners that a new private message is available.
     *
     * @param message the new {@link org.androidnerds.app.aksunai.irc.Message}
     * @param messageList the {@link org.androidnerds.app.aksunai.irc.Private} holding this new message
     */
    public void notifyNewPrivateMessage(Message message, Private messageList) {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about new private message: " + message);
            ml.onNewPrivateMessage(message, messageList);
        }
    }

    /**
     * notifies the listeners that a new notice message is available.
     *
     * @param message the new {@link org.androidnerds.app.aksunai.irc.Message}
     * @param messageList the {@link org.androidnerds.app.aksunai.irc.Notice} holding this new message
     */
    public void notifyNewNoticeMessage(Message message, Notice messageList) {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about new notice message: " + message);
            ml.onNewNoticeMessage(message, messageList);
        }
    }

    /**
     * takes a raw string (server message from the {@link org.androidnerds.app.aksunai.net.ConnectionManager}, formats it,
     * and adds it to the appropriate message list.
     *
     * @param message a String containg the raw message received from the connection
     */
    public void receiveMessage(String message) {
    }

    /**
     * sends a formatted message, by {@link org.androidnerds.app.aksunai.irc.UserMessage},
     * to the server through the {@link org.androidnerds.app.aksunai.net.ConnectionManager}.
     *
     * @param message a string formatted by {@link org.androidnerds.app.aksunai.irc.UserMessage}
     */
    public void sendMessage(String message) {
        mConnectionManager.sendMessage(this, message);
    }
}

