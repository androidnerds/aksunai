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
 * Server is the holder for everything related to the server, its messages, notices, channels, private messages and nick name
 * <p>
 * It has the following input/output points:
 * <ul>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server#receiveMessage}: takes a raw string from the ConnectionManager</li>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server#sendMessage}: sends a raw string to the ConnectionManager</li>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server.MessageListListener}: listeners may register with {@link org.androidnerds.app.aksunai.irc.Server#setMessageListListener}</li>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server.IRCListener}: listeners may register with {@link org.androidnerds.app.aksunai.irc.Server#setIRCListener}</li>
 *     <li>{@link org.androidnerds.app.aksunai.irc.Server#userMessage}: takes a user string, formats it, and sends it to the connection manager</li>
 * </ul>
 */
public class Server extends MessageList {
    private ConnectionManager mConnectionManager;
    public String mNick;
    private List<MessageListListener> mMessageListListeners;
    private List<IRCListener> mIRCListeners;
    public Map<String, MessageList> mMessageLists;

    /**
     * Class constructor.
     *
     * @param cm the connection manager
     * @param name a String, used as the key to store and retrieve this server
     */
    public Server(ConnectionManager cm, String name) {
        super(Type.SERVER, name);
        this.mConnectionManager = cm;
        this.mMessageListListeners = Collections.synchronizedList(new ArrayList<MessageListListener>());
        this.mIRCListeners = Collections.synchronizedList(new ArrayList<IRCListener>());
        this.mMessageLists = Collections.synchronizedMap(new HashMap<String, MessageList>());

        // store this very MessageList in the list of MessageList
        mMessageLists.put(name, this);
    }

    /**
     * MessageList Listener. Listeners must implement the following methods:
     * <ul>
     *     <li>public void onNewMessageList(String serverName, String messageListName);</li>
     *     <li>public void onLeave(String serverName, String messageListName);</li>
     * </ul>
     */
    public interface MessageListListener {
        public void onNewMessageList(String serverName, String messageListName);
        public void onLeave(String serverName, String messageListName);
    }

    /**
     * registers as a MessageList listener.
     */
    public void setMessageListListener(MessageListListener mll) {
        mMessageListListeners.add(mll);
    }

    /**
     * notifies the listeners that a new message list is available.
     *
     * @param name the name of the {@link org.androidnerds.app.aksunai.irc.MessageList} holding this new message
     */
    public void notifyNewMessageList(String name) {
        for (MessageListListener mll: mMessageListListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about new message list: " + name);
            mll.onNewMessageList(this.mName, name);
        }
    }

    /**
     * notifies the listeners that the user left a channel
     *
     * @param name the name of the Channel left
     */
    public void notifyLeave(String name) {
        for (MessageListListener mll: mMessageListListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners about a channel left: " + name);
            mll.onLeave(this.mName, name);
        }
    }

    /**
     * IRC Listener. Listeners must implement the following methods:
     * <ul>
     *     <li>public void onNickInUse(String serverName);</li>
     *     <li>public void onConnected(String serverName);</li>
     * </ul>
     */
    public interface IRCListener {
        public void onNickInUse(String serverName);
        public void onConnected(String serverName);
    }

    /**
     * registers as a IRC listener.
     */
    public void setIRCListener(IRCListener il) {
        mIRCListeners.add(il);
    }

    /**
     * notifies the listeners that the nickname is already in use
     */
    public void notifyNickInUse() {
        for (IRCListener il: mIRCListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners that the nickname is already in use");
            il.onNickInUse(this.mName);
        }
    }

    /**
     * notifies the listeners that the user is connected
     */
    public void notifyConnected() {
        for (IRCListener il: mIRCListeners) {
            if (AppConstants.DEBUG) Log.d(AppConstants.IRC_TAG, "Notifying listeners that the user is connected");
            il.onConnected(this.mName);
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

            mlist = mMessageLists.get(msg.mParameters[1]);
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
                    (ml.mType == MessageList.Type.PRIVATE && ml.mName.equals(msg.mSender))) { /* private message with this user */

                    if (ml.mType == MessageList.Type.CHANNEL) {
                        ((Channel) ml).removeUser(msg.mSender);
                        ((Channel) ml).addUser(msg.mText);
                    } else { /* change the title of the Private */
                        mMessageLists.remove(ml.mName);
                        ml.mName = msg.mText;
                        mMessageLists.put(ml.mName, ml);
                    }
                    storeAndNotify(msg, ml);
                }
            }
        case JOIN:
            if (msg.mSender.equals(mNick)) {
                mMessageLists.put(msg.mText, new Channel(msg.mText));
                notifyNewMessageList(mMessageLists.get(msg.mText).mName);
            } else {
                ((Channel) mMessageLists.get(msg.mText)).addUser(msg.mSender);
                storeAndNotify(msg, mMessageLists.get(msg.mText));
            }
            break;
        case QUIT:
            for (MessageList ml: mMessageLists.values()) {
                if ((ml.mType == MessageList.Type.CHANNEL && ((Channel) ml).mUsers.contains(msg.mSender)) || /* channels which have this user */
                    (ml.mType == MessageList.Type.PRIVATE && ml.mName.equals(msg.mSender))) { /* private message with this user */

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
                notifyLeave(channel.mName);
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
                    notifyNewMessageList(mlist.mName);
                }
            } else { /* channel :from_nick PRIVMSG to_channel :text */
                mlist = mMessageLists.get(msg.mParameters[0]);
                if (mlist == null) {
                    mlist = new Channel(msg.mParameters[0]);
                    mMessageLists.put(msg.mParameters[0], mlist);
                    notifyNewMessageList(mlist.mName);
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
                    notifyNewMessageList(mlist.mName);
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
        mlist.notifyNewMessage();
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
     * @param name the title of the active window
     */
    public void userMessage(String message, String name) {
        sendMessage(UserMessage.format(message, name));
    }
}

