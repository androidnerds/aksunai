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
    static public String format(String message) {
        Command command;
        String parameters;
        String text;

        if (message.startsWith("/")) { /* user command */
        } else { /* standard PRIVMSG */
        }
        String formatted = "";

        /*formatted = command.toString().toUpperCase();

        if (parameters != null && !parameters.equals("")) {
            formatted += " " + parameters;
        }

        if (text != null && !text.equals("")) {
            formatted += " :" + text;
        }
        */

        return formatted;
    }
}

