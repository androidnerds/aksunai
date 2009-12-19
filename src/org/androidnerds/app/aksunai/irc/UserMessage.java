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

import org.androidnerds.app.aksunai.util.AppConstants;

/**
 * UserMessage is used to format a user typed message for a
 * {@link org.androidnerds.app.aksunai.irc.Server} to send to to the actual connected server
 * (through the {@link org.androidnerds.app.aksunai.net.ConnectionManager}).
 */
public class UserMessage {
    /**
     * Returns the formatted string to send to the server.
     *
     * @param message a String, written by the user, to be formatted to send to a server
     * @return the formatted string to send to the server
     */
    static public String format(String message, String title) {
        String formatted = "";

        if (message.startsWith("/")) { /* user command */
            Command command;
            String parameters;
            String text;

            // TODO: format the user message according to the command
        } else { /* standard PRIVMSG */
            formatted = "PRIVMSG " + title + " :" + message;
        }

        return formatted;
    }
}

