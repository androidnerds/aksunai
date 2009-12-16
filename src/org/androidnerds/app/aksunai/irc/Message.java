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

/**
 * Message is the basis of the IRC stack. It parses a message from the server (a line) into:
 * <ul>
 *     <li>sender: may be empty if it's a server message</li>
 *     <li>command: a {@link org.androidnerds.app.aksunai.irc.Command}</li>
 *     <li>parameters: the receiver, the mode, the channel...</li>
 *     <li>data: text message that appears after a ":", usually a the content of a privmsg, notice, part message, topic...</li>
 * </ul>
 *
 * Messages are instanciated by a {@link org.androidnerds.app.aksunai.Server}.
 */
public class Message {
    public Command mCommand;
    public String mSender;
    public String mParameters;
    public String mData;
    public long mTimestamp;

    /**
     * Class constructor. Parses a raw server message.
     */
    Message(String line) {
        // TODO: regex to split the line nicely into tokens
        
        this.mTimestamp = new Date().getTime();
    }
}
