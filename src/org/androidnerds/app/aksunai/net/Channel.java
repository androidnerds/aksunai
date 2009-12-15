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

import java.util.ArrayList;

public class Channel {

    public enum TYPE { PM, SERVER, CHANNEL }

    public TYPE type;
    public ArrayList<String> conversation;
    public ArrayList<String> users;
    public String name;
    public String topic;
    
    private static final int SCROLLBACK_SIZE = 30;

    public Channel(String name, TYPE type) {
        conversation = new ArrayList<String>();
        users = new ArrayList<String>();
        this.name = name;
        this.type = type;
    }

    public void addLine(String line) {
        if (line != null) {
            if (line != "") {
                conversation.add(line);
            }
        }

        if (conversation.size() > SCROLLBACK_SIZE) {
            conversation.remove(0);
        }
    }

    public String toString() {
        return name;
    }
}
