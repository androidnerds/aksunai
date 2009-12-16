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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.androidnerds.app.aksunai.util.AppConstants;
import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.data.ServerDbAdapter;
import org.androidnerds.app.aksunai.ui.Chat;

/* Channels are stored in a Vector right now, that could change as its implemented. */
public class Server {

    public Hashtable<String, Channel> channels;
    ConnectionThread mConnectionThread;
    Thread mThread;
    Handler mHandler;
    Handler userHandler;
    public String motd;
    public String mName;
    public String mUrl;
    public String mNick;
    public String mUsername;
    public String mRealName;
    public int mPort;

    String mPassword;

    public BufferedReader reader;
    public BufferedWriter writer;

    public int state;
    public int connectionState;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_LOGGING_IN = 3;
    public static final int STATE_LOGGED_IN = 4;
    public static final int STATE_READY = 5;
    public static final int STATE_NICK_IN_USE = 6;
    public static final int STATE_NICK_BAD = 7;

    public static final int MSG_UPDATE_CHANNEL = 8;
    public static final int MSG_NEW_WINDOW = 9;
    public static final int MSG_INFO_REQUEST = 10;
    public static final int MSG_UNKNOWN_COMMAND = 11;
    public static final int MSG_DATA_CHANGED = 12;
    public static final int MSG_NO_CHANNEL = 13;

    public static final int RECONNECTING = 1;
    public static final int CONNECTING = 2;

    public static int CONNECTION_STATE;

    public Channel activeChannel;
    private Hashtable<String, String> info;
    private Context mCtx;

    public Server(Context c, long id) {
        channels = new Hashtable<String, Channel>();
        info = new Hashtable<String, String>();
        motd = "Welcome to the Aksunai client!";
        state = Server.STATE_DISCONNECTED;
        mCtx = c;

        //pull the server information from the database.
        ServerDbAdapter db = new ServerDbAdapter(mCtx);
        Cursor cur = db.getItem(id);

        while (cur.moveToNext()) {
            mName = cur.getString(1);
            mUrl = cur.getString(2);
            mUsername = cur.getString(3);
            mNick = cur.getString(4);
            mPassword = cur.getString(5);
            mPort = cur.getInt(6);
            mRealName = cur.getString(7);
        }

        db.release();

        CONNECTION_STATE = CONNECTING;

        Channel chan = new Channel(mName, Channel.TYPE.SERVER);
        activeChannel = chan;
        channels.put(mName, chan);

        //server class is in charge of its ConnectionThread.
        loadThread();
    }

    private void loadThread() {
        mConnectionThread = new ConnectionThread(mUrl, mNick, mUsername, mPassword, mPort, mRealName, this);
        mThread = new Thread(mConnectionThread);
        mThread.start();
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void setUserHandler(Handler handler) {
        userHandler = handler;
    }

    public void freeUserHandler() {
        userHandler = null;
    }

    public void setActiveChannel(String channel) {
        activeChannel = channels.get(channel);
        Message.obtain(mHandler, Server.MSG_NEW_WINDOW, channel).sendToTarget();
    }

    public void closeConnection() {
        sendMessage("/quit");
        mConnectionThread.disconnect();
        
        if (ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
            Message.obtain(mHandler, Server.STATE_DISCONNECTED, mName).sendToTarget();
        }
    }

    public void connectAndLoadState() {
        connectionState = RECONNECTING;
        loadThread();
    }

    //this is the method where we send all the join requests to.
    public void loadState() {
        for (Channel c : channels.values()) {
            if (c.type != Channel.TYPE.PM) {
                sendMessage("/join " + c.name);
            }
        }

        connectionState = CONNECTING;
    }

    public void joinChannel(String channel) {
        Channel chan = new Channel(channel, Channel.TYPE.CHANNEL);
        activeChannel = chan;
        channels.put(channel, chan);

        if (activeChannel.name.equals(chan.name) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
            Message.obtain(mHandler, Server.MSG_NEW_WINDOW, channel).sendToTarget();
        }
    }

    public void startPM(String user) {
        Channel chan = new Channel(user, Channel.TYPE.PM);

        //removed the activeChannel assignment because there isn't a notification being sent. race condition.
        //activeChannel = chan;

        channels.put(user, chan);

        //alert the user their nick has been said.
        Intent i = new Intent(mCtx, Chat.class);
        i.putExtra("name", mName);
        i.putExtra("channel", user);

        NotificationManager nm = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, i, 0);
        Notification notif = new Notification(android.R.drawable.stat_notify_chat, mCtx.getString(R.string.new_private_chat), System.currentTimeMillis());
        notif.setLatestEventInfo(mCtx, user, mCtx.getString(R.string.new_private_chat), contentIntent);
        notif.vibrate = new long[] { 100, 250, 100, 250 };

        nm.notify(R.string.incoming_message, notif);
    }

    public void openNewPM(String user) {
        startPM(user);
        activeChannel = channels.get(user);

        if (activeChannel.name.equals(user) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
            Message.obtain(mHandler, Server.MSG_NEW_WINDOW, user).sendToTarget();
        }
    }

    public void partChannel(String channel) {
        if (channels.containsKey(channel)) {
            channels.remove(channel);

            Enumeration<String> e = channels.keys();
            Object o = e.nextElement();
            Channel chan = channels.get(o);
            activeChannel = chan;

            if (activeChannel.name.equals(chan.name) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                Message.obtain(mHandler, Server.MSG_NEW_WINDOW, activeChannel.name).sendToTarget();
            }
        }
    }

    public void sendMessage(String msg) {
        if (msg.startsWith("/")) {
            if (msg.startsWith("/me")) {
                try {
                    String tmp = "PRIVMSG " + activeChannel + " :" + '\001' + "ACTION " + msg.substring(4) + '\001' + "\r\n";
                    writer.write(tmp);
                    writer.flush();
                } catch (IOException e) {
                    if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught sending action message to server.");
                }
            } else if (msg.startsWith("/close")) {
                if (activeChannel.type == Channel.TYPE.SERVER) {
                    return;
                } else if (activeChannel.type == Channel.TYPE.PM) {
                    partChannel(activeChannel.name);
                    return;
                } else {
                    try {
                        String tmp = "PART " + activeChannel.name + " :User Left Channel\r\n";
                        writer.write(tmp);
                        writer.flush();

                        partChannel(activeChannel.name);
                    } catch (IOException e) {
                        if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught while parting the channel " + activeChannel.name);
                    }

                    return;
                }
            } else if (msg.startsWith("/join")) {
                try {
                    String tmp = "JOIN " + msg.substring(5) + "\r\n";
                    writer.write(tmp);
                    writer.flush();

                    if (connectionState != RECONNECTING) {
                        joinChannel(msg.substring(5).trim());
                    }
                } catch (IOException e) {
                    if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught while joining the channel " + activeChannel.name);
                }
            } else if (msg.startsWith("/whois")) {
                try {
                    String tmp = "WHOIS" + msg.substring(6) + "\r\n";
                    writer.write(tmp);
                    writer.flush();
                } catch (IOException e) {
                    if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught while retreiving whois for " + msg.substring(6));
                }
            } else if (msg.startsWith("/quit")) {
                try {
                    String tmp = "QUIT " +msg.substring(5) + "\r\n";
                    writer.write(tmp);
                    writer.flush();
                } catch (IOException e) {
                    if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught while quitting the server: " + mName);
                }
            } else {
                if (msg.indexOf(" ") == -1) {
                    msg = msg.substring(1);
                } else {
                    msg = msg.substring(1, msg.indexOf(" "));
                }

                if (ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                    Message.obtain(mHandler, Server.MSG_UNKNOWN_COMMAND, msg).sendToTarget();
                }
            }
        } else {
            if (activeChannel.type == Channel.TYPE.SERVER) {
                Message.obtain(mHandler, Server.MSG_NO_CHANNEL, "no channel").sendToTarget();
                return;
            }

            try {
                String tmp = "PRIVMSG " + activeChannel.name + " :" + msg + "\r\n";
                writer.write(tmp);
                writer.flush();

                Channel chan = channels.get(activeChannel.name);
                chan.conversation.add(mNick + " " + msg);

                if (activeChannel.name.equals(activeChannel.name) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                    Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, "me").sendToTarget();
                }
            } catch (IOException e) {
                if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "IOException caught while sending message to channel " + activeChannel.name);
            }
        }
    }

    /* This method parses the irc messages from the server. */
    public void getLine(String line) {
        if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "Message: " + line);

        String[] splittedMsg;

        if (line.startsWith(":")) {
            try {
                splittedMsg = line.split(" ", 4);

                Integer.parseInt(splittedMsg[1]); // if it's a numeric command, it's a server message

                String message = splittedMsg[3];

                if (message.startsWith(":")) {
                    message = message.substring(1);
                }

                Channel chan = channels.get(mName);
                chan.conversation.add("* " + message); // example: ":orwell.freenode.net 001 OhanAndroid :Welcome to the freenode IRC Network OhanDroid"

                if (activeChannel.name.equals(mName) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                    Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, mName).sendToTarget();
                }
            } catch( NumberFormatException e) {}
        } else { // server notice
            splittedMsg = line.split(" ", 3);
            String message = splittedMsg[2];

            if (message.startsWith(":")) {
                message = message.substring(1);
            }

            Channel chan = channels.get(mName);
            chan.conversation.add("* " + message); // example: "NOTICE AUTH :*** Looking up your hostname..."

            if (activeChannel.name.equals(mName) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, mName).sendToTarget();
            }
        }

        String command = line.split(" ")[1].trim();

        if (command.equals("001")) {
            //connection notification.
            if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, "Connected notification received.");
            Message.obtain(mHandler, Server.STATE_CONNECTED, "connected").sendToTarget();
        } else if (command.equals("353")) {
            int loc = line.indexOf(line.split(" ")[5]);

            String[] names = line.substring(loc + 1).split(" ");

            for (String s : names) {
                activeChannel.users.add(s.trim());
            }
        } else if (command.equals("901")) {
            //we don't need any of the information just that a 901 was received.
            if (ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                Message.obtain(mHandler, Server.STATE_LOGGED_IN, "authenticated").sendToTarget();
            }
        } else if (command.equals("311")) {
            info.clear();
            String[] parts = line.split(" ");
            String nick = parts[3].trim();
            String username = parts[4].trim() + "@" + parts[5].trim();
            String realname = line.substring(line.indexOf(parts[7].substring(1)));

            info.put("nick", nick);
            info.put("username", username);
            info.put("realname", realname);
        } else if (command.equals("312")) {
            info.put("server", line.split(" ")[4].trim());
        } else if (command.equals("319")) {
            info.put("channels", line.substring(line.indexOf(line.split(" ")[4].substring(1).trim())));
        } else if (command.equals("318")) {
            Message.obtain(mHandler, Server.MSG_INFO_REQUEST, info).sendToTarget();
        } else if (command.equals("332")) {
        	String[] parts = line.split(" ");
        	String channel = parts[3];
        	String message = line.substring(line.indexOf(parts[4].substring(1).trim()));
        	
        	Channel chan = channels.get(channel);
        	chan.conversation.add("topic " + message);
        	chan.topic = message;
        	
        	if (activeChannel.name == chan.name && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
        		Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, channel).sendToTarget();
        	}
        } else if (command.equals("PRIVMSG")) {
            String dest = line.split(" ")[2].trim();
            String user = line.substring(1, line.indexOf("!")).trim();
            int dist = command.length() + dest.length() + 3;
            String message = line.substring(line.indexOf(command) + dist).trim();

            if (dest.equals(mNick)) {
                if (!channels.containsKey(user)) {
                    startPM(user);
                } else {
                    Channel chan = channels.get(user);
                    chan.conversation.add(user + " " + message);

                    //check to see if a users nick has been said.
                    if (message.contains(mNick) && (!activeChannel.name.equals(user) || ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_CLOSED)) {
                        if (!Character.isLetterOrDigit(message.charAt(message.indexOf(mNick) + mNick.length() - 1))) {
                            //alert the user their nick has been said.
                            Intent i = new Intent(mCtx, Chat.class);
                            i.putExtra("name", mName);
                            i.putExtra("channel", user);

                            NotificationManager nm = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
                            PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, i, 0);
                            Notification notif = new Notification(android.R.drawable.stat_notify_chat, message, System.currentTimeMillis());
                            notif.setLatestEventInfo(mCtx, user, message, contentIntent);
                            notif.vibrate = new long[] { 100, 250, 100, 250 };

                            nm.notify(R.string.incoming_message, notif);
                        }
                    }

                    if (activeChannel != null) {
                        if (activeChannel.name.equals(user) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                            Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, user).sendToTarget();
                        }
                    }
                }
            } else {
                if (!channels.containsKey(dest)) {
                    joinChannel(dest);
                }

                Channel chan = channels.get(dest);
                chan.conversation.add(user + " " + message);

                //check to see if a users nick has been said.
                if (message.contains(mNick) && (!activeChannel.name.equals(dest) || ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_CLOSED)) {
                    if (!Character.isLetterOrDigit(message.charAt(message.indexOf(mNick) + mNick.length() - 1))) {
                        //alert the user their nick has been said.
                        Intent i = new Intent(mCtx, Chat.class);
                        i.putExtra("name", mName);
                        i.putExtra("channel", dest);

                        NotificationManager nm = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
                        PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, i, 0);
                        Notification notif = new Notification(android.R.drawable.stat_notify_chat, message, System.currentTimeMillis());
                        notif.setLatestEventInfo(mCtx, user, message, contentIntent);
                        notif.vibrate = new long[] { 100, 250, 100, 500 };

                        nm.notify(R.string.incoming_message, notif);
                    }
                }

                if (activeChannel != null) {
                    if (activeChannel.name.equals(dest) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                        Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, dest).sendToTarget();
                    }
                }
            }
        } else if (command.equals("JOIN")) {
            String nick = line.substring(1, line.indexOf("!")).trim();
            String username = line.substring(line.indexOf("!") + 1, line.indexOf(" ")).trim();
            String room = line.split(" ")[2].substring(1).trim();

            if (channels.containsKey(room) && !nick.equals(mNick)) {
                Channel chan = channels.get(room);
                chan.conversation.add(nick + " JOIN " + nick + " [" + username + "] has entered the room.");

                chan.users.add(nick);

                if (activeChannel.name.equals(room) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                    Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, room).sendToTarget();
                }

                if (userHandler != null) {
                    Message.obtain(userHandler, Server.MSG_DATA_CHANGED, room).sendToTarget();
                }
            }
        } else if (command.equals("PART")) {
            String nick = line.substring(1, line.indexOf("!")).trim();
            String room = line.split(" ")[2].trim();
            String msg = line.split(" ")[3].trim();

            if (msg.equals(":")) {
                msg = "";
            }

            if (channels.containsKey(room) && !nick.equals(mNick)) {
                Channel chan = channels.get(room);
                chan.conversation.add(nick + " PART " + nick + "has left the room (" + msg +")");

                if (AppConstants.DEBUG) Log.d(AppConstants.NET_TAG, nick + " PART " + nick + "has left the room (" + msg +")");
                chan.users.remove(nick);

                if (activeChannel.name.equals(room) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                    Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, room).sendToTarget();
                }

                if (userHandler != null) {
                    Message.obtain(userHandler, Server.MSG_DATA_CHANGED, room).sendToTarget();
                }
            }
        } else if (command.equals("QUIT")) {
            String nick = line.substring(1, line.indexOf("!")).trim();
            String msg = line.split(" ")[2].trim();

            if (msg.equals(":")) {
                msg = "";
            }

            //go through all channels and remove the user.
            for (Channel c : channels.values()) {
                if (c.users.contains(nick)) {
                    c.users.remove(nick);
                    c.conversation.add(nick + " PART " + nick + "has left the room (quit: " + msg + ")");
                }

                if (activeChannel.name.equals(c.name) && ConnectionService.STATE_CHAT_WINDOW == Chat.STATE_WINDOW_OPEN) {
                    Message.obtain(mHandler, Server.MSG_UPDATE_CHANNEL, c.name).sendToTarget();
                }

                if (userHandler != null && activeChannel.name.equals(c.name)) {
                    Message.obtain(userHandler, Server.MSG_DATA_CHANGED, "quit").sendToTarget();
                }

            }
        } else if (command.equals("MODE")) {

        }
    }

    public String toString() {
        return mName + " " + mUrl;
    }
}
