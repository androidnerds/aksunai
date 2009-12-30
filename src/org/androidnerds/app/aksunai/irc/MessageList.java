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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.androidnerds.app.aksunai.util.AppConstants;

/**
 * MessageList is the base class for {@link org.androidnerds.app.aksunai.irc.Server},
 * {@link org.androidnerds.app.aksunai.irc.Channel} and {@link org.androidnerds.app.aksunai.irc.Private}.
 * <p>
 * {@link org.androidnerds.app.aksunai.irc.Server.NewMessageListener}: listeners may register with {@link org.androidnerds.app.aksunai.irc.Server#setOnNewMessageListener}.
 * <p>
 * It holds the list of {@link org.androidnerds.app.aksunai.irc.Message} and a name to identify it
 */
public class MessageList {
    
    public enum Type { SERVER, CHANNEL, PRIVATE, NOTICE }

    public String mName;
    public Type mType;
    public List<Message> mMessages;
    private List<NewMessageListener> mNewMessageListeners;

    /**
     * Class constructor.
     *
     * @param name a String, used as window title by the ChatManager
     */
    public MessageList(Type type, String name) {
        this.mType = type;
        this.mName = name;
        this.mMessages = Collections.synchronizedList(new ArrayList<Message>());
        this.mNewMessageListeners = Collections.synchronizedList(new ArrayList<NewMessageListener>());
    }

    /**
     * New Message Listener. Listeners must implement the following method:
     *     public void onNewMessage(Message message, MessageList mlist);
     */
    public interface NewMessageListener {
        public void onNewMessage(String message, String server, String list);
    }

    /**
     * registers as a new message listener.
     */
    public void setOnNewMessageListener(NewMessageListener nml) {
        mNewMessageListeners.add(nml);
    }

    /**
     * notifies the listeners that a new message is available.
     */
    public void notifyNewMessage(String message, String server, String list) {
        synchronized(mNewMessageListeners) {
            for (NewMessageListener nml: mNewMessageListeners) {
                nml.onNewMessage(message, server, list);
            }
        }
    }

    public String toString() {
        return "MessageList type=" + mType + " mName=" + mName;
    }
}

