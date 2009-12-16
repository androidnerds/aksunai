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
 * UserMessage is used to format a command (and its parameters) nicely for a
 * {@link org.androidnerds.app.aksunai.irc.Server} to send to to the actual connected server
 * (through the {@link org.androidnerds.app.aksunai.net.ConnectionManager}).
 */
public class UserMessage {
    /**
     * Returns the formatted string to send to the server.
     *
     * @param command a Command
     * @param parameters a string containing the parameters (may be empty or null)
     * @param data a string containing the actual text data (may be empty or null)
     * @return the formatted string to send to the server
     */
    static public String format(Command command, String parameters, String data) {
        String formatted = command.toString().toUpperCase();

        if (parameters != null && !parameters.equals("")) {
            formatted += " " + parameters;
        }

        if (data != null && !data.equals("")) {
            formatted += " :" + data;
        }

        return formatted;
    }
}

