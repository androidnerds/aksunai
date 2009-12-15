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

import android.util.Log;

import org.androidnerds.app.aksunai.MyConfig;

public enum Command {
    PASS ("pass"),
    NICK ("nick"),
    USER ("user"),
    SERVER ("server"),
    OPER ("oper"),
    QUIT ("quit"),
    SQUIT ("squit"),

    /* channel operations */
    JOIN ("join"),
    PART ("part"),
    MODE ("mode"),
    TOPIC ("topic"),
    NAMES ("names"),
    LIST ("list"),
    INVITE ("invite"),
    KICK ("kick"),

    /* server queries and commands */
    VERSION ("version"),
    STATS ("stats"),
    LINKS ("links"),
    TIME ("time"),
    CONNECT ("connect"),
    TRACE ("trace"),
    ADMIN ("admin"),
    INFO ("info"),

    /* sending messages */
    PRIVMSG ("privmsg"),
    NOTICE ("notice"),

    /* user based queries */
    WHO ("who"),
    WHOIS ("whois"),
    WHOWAS ("whowas"),

    /* miscellaneous messages */
    KILL ("kill"),
    PING ("ping"),
    PONG ("pong"),
    ERROR ("error"),

    /* optionals */
    AWAY ("away"),
    REHASH ("rehash"),
    RESTART ("restart"),
    SUMMON ("summon"),
    USERS ("users"),
    WALLOPS ("wallops"),
    USERHOST ("userhost"),
    ISON ("ison");

    private final String str; // string representation

    Command(String string) {
        this.str = string;
    }

    public boolean equalsIgnoreCase(String command) {
        return command.toLowerCase().equals(this.str.toLowerCase());
    }
}

