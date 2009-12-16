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

/**
 * Server is the holder for everything related to the server, its messages, notices, channels, private messages,
 * nick name, username, real name...
 * <p>
 * It has three input/output points:
 * <ul>
 *     <li>receiveMessage: takes a raw string from the ConnectionManager</li>
 *     <li>sendMessage: sends a raw string to the ConnectionManager</li>
 *     <li>updateUI: notify the ChatManager that some messages were received, and for wich MessageList instance</li>
 * </ul>
 */
public class Server extends MessageList {
    public String mNick;

    /**
     * Class constructor.
     *
     * @param title a String, used as window title by the ChatManager
     */
    public Server(String title) {
        super(title);
    }
}


