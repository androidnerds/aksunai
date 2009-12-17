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

/**
 * Command is the enum used by {@link org.androidnerds.app.aksunai.irc.Message} and
 * {@link org.androidnerds.app.aksunai.irc.UserMessage}.
 * <p>
 * It's used in switches, to style messages in windows, 
 * and the {@link org.androidnerds.app.aksunai.irc.Command#toString}
 * method will return the actual command to send to the irc server.
 */
public enum Command {
    /* server initiated messages, no command */
    NONE (""),

    /* connection registration */
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
    ISON ("ison"),
    
    /* unknown command */
    UNKNOWN ("unknown");


    private final String mStr;

    /**
     * Enum constructor. Sets the internal string representation of each command.
     */
    Command(String string) {
        this.mStr = string;
    }

    /**
     * Returns true if the given string is equal to the internal representation of this command.
     * The check is case insensitive.
     *
     * @param command a string to be compared to
     * @return true if the parameter is equal to the string representation, case insensitively
     */
    public boolean equalsIgnoreCase(String command) {
        return command.toLowerCase().equals(this.mStr);
    }

    /**
     * Returns the string representation of this command.
     *
     * @return the string representation of this command
     */
    public String toString() {
        return this.mStr;
    }
}

