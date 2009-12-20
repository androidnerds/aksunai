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
            UserCommand command;
            String parameters;
            String text;

            message = message.substring(1); /* drop the leading "/" */

            String cmd_str = head(message); /* the first "word" is the command */
            String params = tail(message);

            Command cmd = Command.UNKNOWN;
            for (Command c: Command.values()) {
                if (c.startsWithIgnoreCase(cmd_str)) { /* using "startsWithIgnoreCase allows the command /j to be matched with /join */
                    cmd = c;
                }
            }

            switch (cmd) {
            case JOIN:
                formatted = "JOIN " + params;
                break;
            case PART:
                formatted = "PART " + params;
                break;
            case PRIVMSG:
                formatted = "PRIVMSG " + head(params) + " :" + tail(params);
                break;
            case QUIT:
                if (params != null && !params.equals("")) {
                    formatted = "QUIT :" + params;
                } else {
                    formatted = "QUIT :leaving";
                }
                break;
            default: /* command unknown or not implemented: try to send it as is */
                formatted = message; 
                break;
            }
        } else { /* standard PRIVMSG */
            formatted = "PRIVMSG " + title + " :" + message;
        }

        return formatted;
    }

    /**
     * returns the first word.
     *
     * @param the string to get the head from
     * @return the first word from the given string
     */
    public static String head(String msg) {
        return msg.split(" ", 2)[0];
    }

    /**
     * returns everything except the first word.
     *
     * @param the string to get the tail from
     * @return everything except the first work from the given string
     */
    public static String tail(String msg) {
        return msg.split(" ", 2)[1];
    }
}

