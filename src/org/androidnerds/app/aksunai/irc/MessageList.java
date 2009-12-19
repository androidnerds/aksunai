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
 * It holds the list of {@link org.androidnerds.app.aksunai.irc.Message} and a title (used as window title)
 */
public class MessageList {
    
    public enum Type { SERVER, CHANNEL, PRIVATE, NOTICE }

    public String mTitle;
    public Type mType;
    public List<Message> mMessages;

    /**
     * Class constructor.
     *
     * @param title a String, used as window title by the ChatManager
     */
    public MessageList(Type type, String title) {
        this.mType = type;
        this.mTitle = title;
        this.mMessages = Collections.synchronizedList(new ArrayList<Message>());
    }

    public String toString() {
        return "MessageList type=" + mType + " title=" + mTitle;
    }
}

