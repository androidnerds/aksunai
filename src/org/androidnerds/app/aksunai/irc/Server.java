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
 * It has four input/output points:
 * <ul>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server#receiveMessage}: takes a raw string from the ConnectionManager</li>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server#sendMessage}: sends a raw string to the ConnectionManager</li>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server.MessageListener}: listeners may register with {@link org.androidnerds.app.aksunai.irc.Server#setOnNewMessageListener}</li>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server#userMessage}: takes a user string, formats it, and sends it via sendMessage</li>
 * </ul>
 */
public class Server extends MessageList {
    private ConnectionManager mConnectionManager;
    public String mNick;
    private List<MessageListener> mListeners;
    public Map<String, MessageList> mMessageLists;

    /**
     * Class constructor.
     *
     * @param title a String, used as window title by the ChatManager
     */
    public Server(ConnectionManager cm, String title) {
        super(Type.SERVER, title);
        this.mType = Type.SERVER;
        this.mConnectionManager = cm;
        this.mListeners = Collections.synchronizedList(new ArrayList<MessageListener>());
        this.mMessageLists = Collections.synchronizedMap(new HashMap<String, MessageList>());

        notifyNewMessageList(this);
    }

    /**
     * Message Listener. Listeners must implement the following methods:
     * <ul>
     *     <li>public void onNewMessageList(MessageList mlist);</li>
     *     <li>public void onNewMessage(Message message, MessageList mlist);</li>
     *     <li>public void onQuit();</li>
     *     <li>public void onLeave(String title);</li>
     * </ul>
     */
    public interface MessageListener {
        public void onNewMessageList(MessageList mlist);
        public void onNewMessage(Message message, MessageList mlist);
        public void onQuit();
        public void onLeave(String title);
    }

    /**
     * registers as a message listener.
     */
    public void setOnNewMessageListener(MessageListener ml) {
        mListeners.add(ml);
    }

    /**
     * notifies the listeners that a new message list is available.
     *
     * @param messageList the {@link org.androidnerds.app.aksunai.irc.MessageList} holding this new message
     */
    public void notifyNewMessageList(MessageList mlist) {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about new message list: " + mlist);
            ml.onNewMessageList(mlist);
        }
    }

    /**
     * notifies the listeners that a new message is available.
     *
     * @param message the new {@link org.androidnerds.app.aksunai.irc.Message}
     * @param messageList the {@link org.androidnerds.app.aksunai.irc.MessageList} holding this new message
     */
    public void notifyNewMessage(Message message, MessageList mlist) {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about new message: " + message);
            ml.onNewMessage(message, mlist);
        }
    }

    /**
     * notifies the listeners that the user disconnected
     */
    public void notifyQuit() {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners that the user has quit");
            ml.onQuit();
        }
    }

    /**
     * notifies the listeners that the user left a channel
     *
     * @param title the name of the Channel left
     */
    public void notifyLeave(String title) {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about a channel left: " + title);
            ml.onLeave(title);
        }
    }

    /**
     * takes a raw string (server message from the {@link org.androidnerds.app.aksunai.net.ConnectionManager}, formats it,
     * and adds it to the appropriate message list.
     *
     * @param message a String containg the raw message received from the connection
     */
    public void receiveMessage(String message) {
        Message msg = new Message(message);

        switch (msg.mCommand) {
        case _001:
            mNick = msg.mParameters[0];
            /* fall through */
        case OTHER:
            storeAndNotify(msg, this);
            break;
        case JOIN:
            if (msg.mSender.equals(mNick)) {
                mMessageLists.put(msg.mText, (MessageList) new Channel(msg.mText));
                notifyNewMessageList(mMessageLists.get(msg.mText));
            } else {
                ((Channel) mMessageLists.get(msg.mText)).addUser(msg.mSender);
                storeAndNotify(msg, mMessageLists.get(msg.mText));
            }
            break;
        case QUIT:
            if (msg.mSender.equals(mNick)) {
                notifyQuit();
            } else {
                for (MessageList mlist: mMessageLists.values()) {
                    if ((mlist.mType == MessageList.Type.CHANNEL && ((Channel) mlist).mUsers.contains(msg.mSender)) || /* channels which have this user */
                        (mlist.mType == MessageList.Type.PRIVATE && mlist.mTitle.equals(msg.mSender))) { /* private message with this user */

                        if (mlist.mType == MessageList.Type.CHANNEL) {
                            ((Channel) mlist).removeUser(msg.mSender);
                        }
                        storeAndNotify(msg, mlist);
                    }
                }
            }
            break;
        case PART:
            if (msg.mSender.equals(mNick)) {
                notifyLeave(msg.mParameters[0]);
            } else {
                Channel channel = (Channel) mMessageLists.get(msg.mParameters[0]);
                channel.removeUser(msg.mSender);
                storeAndNotify(msg, channel);
            }
        case PING:
            sendMessage("PONG :" + msg.mText);
            break;
        // TODO: rest of the parsing
        default:
            break;
        }
    }

    /**
     * stores the message in the appropriate MessageList, and notify the listeners.
     *
     * @param message the message to store
     * @param mlist the message list in which to store the message
     */
    private void storeAndNotify(Message message, MessageList mlist) {
        mlist.mMessages.add(message);
        notifyNewMessage(message, mlist);
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

    /**
     * takes a string from the {@link org.androidnerds.app.aksunai.service.ChatManager} and formats it
     * before sending it to the server through the {@link org.androidnerds.app.aksunai.net.ConnectionManager}
     */
    public void userMessage(String message) {
        // TODO: formatting
        sendMessage(message);
    }
}

