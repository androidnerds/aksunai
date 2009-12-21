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
import java.util.Date;
import java.text.SimpleDateFormat;
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
    }

    /**
     * Message Listener. Listeners must implement the following methods:
     * <ul>
     *     <li>public void onNewMessageList(MessageList mlist);</li>
     *     <li>public void onNewMessage(Message message, MessageList mlist);</li>
     *     <li>public void onNickInUse();</li>
     *     <li>public void onLeave(String title);</li>
     *     <li>public void onConnected();</li>
     * </ul>
     */
    public interface MessageListener {
        public void onNewMessageList(Server server, MessageList mlist);
        public void onNewMessage(Server server, Message message, MessageList mlist);
        public void onNickInUse(Server server);
        public void onLeave(Server server, MessageList mlist);
        public void onConnected(Server server);
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
            ml.onNewMessageList(this, mlist);
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
            ml.onNewMessage(this, message, mlist);
        }
    }

    /**
     * notifies the listeners that the nickname is already in use
     */
    public void notifyNickInUse() {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners that the nickname is already in use");
            ml.onNickInUse(this);
        }
    }

    /**
     * notifies the listeners that the user left a channel
     *
     * @param title the name of the Channel left
     */
    public void notifyLeave(MessageList mlist) {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about a channel left: " + mlist);
            ml.onLeave(this, mlist);
        }
    }

    /**
     * notifies the listeners that the user is connected
     */
    public void notifyConnected() {
        for (MessageListener ml: mListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners that the user is connected");
            ml.onConnected(this);
        }
    }

    /**
     * initializes a connection to the server by sending the PASS, NICK and USER commands.
     *
     * @param pass the server password, null if not needed
     * @param nick the nickname
     * @param username the username to register with
     * @param realname the real name of the user
     */
    public void init(String pass, String nick, String username, String realname) {
        if (pass != null && !pass.equals("")) {
            sendMessage("PASS " + pass);
        }
        sendMessage("NICK " + nick);
        sendMessage("USER " + username + " * * :" + realname);
    }

    /**
     * takes a raw string (server message from the {@link org.androidnerds.app.aksunai.net.ConnectionManager}, formats it,
     * and adds it to the appropriate message list.
     *
     * @param message a String containg the raw message received from the connection
     */
    public void receiveMessage(String message) {
        Message msg = new Message(message);
        MessageList mlist;
        Channel channel;

        switch (msg.mCommand) {
        case CONNECTED:
            mNick = msg.mParameters[0];
            storeAndNotify(msg, this);
            notifyConnected();
            break;
        case CHANNEL_TOPIC:
            mlist = mMessageLists.get(msg.mParameters[msg.mParameters.length -1]);
            storeAndNotify(msg, mlist);
            break;
        case CHANNEL_TOPIC_SETTER:
            /* decode the timestamp to have the human readable date when the topic was set */
            String timestamp = msg.mParameters[3];

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
            Date date = new Date(Integer.parseInt(timestamp));
            msg.mText = msg.mParameters[2]+ ": " + formatter.format(date); /* nick: date */

            mlist = mMessageLists.get(msg.mParameters[msg.mParameters.length -1]);
            storeAndNotify(msg, mlist);
            break;
        case USERS:
            String[] users = msg.mText.split(" ");
            channel = (Channel) mMessageLists.get(msg.mParameters[msg.mParameters.length -1]);
            for (String user: users) {
                channel.addUser(user);
            }
            break;
        case NO_NICK:
        case ERRONEUS_NICK:
        case NICK_IN_USE:
        case NICK_COLLISION:
            notifyNickInUse();
            /* fall through */
        case OTHER:
            storeAndNotify(msg, this);
            break;
        case NICK:
            if (msg.mSender.equals(mNick)) {
                mNick = msg.mText;
            }
            for (MessageList ml: mMessageLists.values()) {
                if ((ml.mType == MessageList.Type.CHANNEL && ((Channel) ml).mUsers.contains(msg.mSender)) || /* channels which have this user */
                    (ml.mType == MessageList.Type.PRIVATE && ml.mTitle.equals(msg.mSender))) { /* private message with this user */

                    if (ml.mType == MessageList.Type.CHANNEL) {
                        ((Channel) ml).removeUser(msg.mSender);
                        ((Channel) ml).addUser(msg.mText);
                    } else { /* change the title of the Private */
                        mMessageLists.remove(ml.mTitle);
                        ml.mTitle = msg.mText;
                        mMessageLists.put(ml.mTitle, ml);
                    }
                    storeAndNotify(msg, ml);
                }
            }
        case JOIN:
            if (msg.mSender.equals(mNick)) {
                mMessageLists.put(msg.mText, new Channel(msg.mText));
                notifyNewMessageList(mMessageLists.get(msg.mText));
            } else {
                ((Channel) mMessageLists.get(msg.mText)).addUser(msg.mSender);
                storeAndNotify(msg, mMessageLists.get(msg.mText));
            }
            break;
        case QUIT:
            for (MessageList ml: mMessageLists.values()) {
                if ((ml.mType == MessageList.Type.CHANNEL && ((Channel) ml).mUsers.contains(msg.mSender)) || /* channels which have this user */
                    (ml.mType == MessageList.Type.PRIVATE && ml.mTitle.equals(msg.mSender))) { /* private message with this user */

                    if (ml.mType == MessageList.Type.CHANNEL) {
                        ((Channel) ml).removeUser(msg.mSender);
                    }
                    storeAndNotify(msg, ml);
                }
            }
            break;
        case PART:
            channel = (Channel) mMessageLists.get(msg.mParameters[0]);
            if (msg.mSender.equals(mNick)) {
                notifyLeave(channel);
            } else {
                channel.removeUser(msg.mSender);
                storeAndNotify(msg, channel);
            }
        case PRIVMSG:
            if (msg.mParameters[0].equals(mNick)) { /* private message :from_nick PRIVMSG to_nick :text */
                mlist = mMessageLists.get(msg.mSender);
                if (mlist == null) {
                    mlist = new MessageList(MessageList.Type.PRIVATE, msg.mSender);
                    mMessageLists.put(msg.mSender, mlist);
                    notifyNewMessageList(mlist);
                }
            } else { /* channel :from_nick PRIVMSG to_channel :text */
                mlist = mMessageLists.get(msg.mParameters[0]);
                if (mlist == null) {
                    mlist = new Channel(msg.mParameters[0]);
                    mMessageLists.put(msg.mParameters[0], mlist);
                    notifyNewMessageList(mlist);
                }
            }
            storeAndNotify(msg, mlist);
            break;
        case NOTICE:
            if (msg.mSender == null) { /* server notice */
                storeAndNotify(msg, this);
            } else { /* user notice */
                mlist = mMessageLists.get("notices"); /* only one MessageList to hold all the user notices */
                if (mlist == null) {
                    mlist = new MessageList(MessageList.Type.NOTICE, msg.mSender);
                    mMessageLists.put(msg.mSender, mlist);
                    notifyNewMessageList(mlist);
                }
                storeAndNotify(msg, mlist);
            }
            break;
        case PING:
            sendMessage("PONG :" + msg.mText);
            break;
        // TODO: rest of the parsing
        default:
            storeAndNotify(msg, this);
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
     *
     * @param message an unformatted string written by the user
     * @param title the title of the active window
     */
    public void userMessage(String message, String title) {
        sendMessage(UserMessage.format(message, title));
    }
}

