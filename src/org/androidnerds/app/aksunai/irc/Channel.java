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
import java.util.SortedSet;
import java.util.TreeSet;

import org.androidnerds.app.aksunai.util.AppConstants;

/**
 * Channel holds a topic, an alphabetically sorted user list, and a list of messages.
 */
public class Channel extends MessageList {
    public SortedSet<String> mUsers;

    /**
     * Class constructor.
     *
     * @param title a String, used as window title by the ChatManager
     * @param topic a String, the topic and its setter
     */
    public Channel(String title) {
        super(Type.CHANNEL, title);
        this.mUsers = Collections.synchronizedSortedSet(new TreeSet<String>());
    }

    /**
     * adds a nickname to the list of users.
     *
     * @param nick the nickname of the new user
     */
    public void addUser(String nick) {
        this.mUsers.add(nick);
    }

    /**
     * removes a nickname fromt he list of users.
     *
     * @param nick the nickname of the user to remove
     */
    public void removeUser(String nick) {
        this.mUsers.remove(nick);
    }
}

