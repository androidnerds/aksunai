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
 * UserCommand is the enum used by {@link org.androidnerds.app.aksunai.irc.UserMessage}.
 * <p>
 * It's used in switches, to format the message for the server
 * and the {@link org.androidnerds.app.aksunai.irc.UserCommand#toString}
 * method will return the actual command to send to the irc server.
 */
public enum UserCommand {
    /* connection registration */
    PASS ("PASS"),                  // PASS password
    NICK ("NICK"),                  // NICK newnick
    USER ("USER"),                  // USER nick hotsname servername :realname
    OPER ("OPER"),                  // OPER user password
    QUIT ("QUIT"),                  // QUIT [reason]

    /* channel operations */
    JOIN ("JOIN"),                  // JOIN channel{,channel} [key{,key}]
    PART ("PART"),                  // PART channel[,channel] [:reason]
    MODE ("MODE"),                  // (MODE channel mode [nick] | MODE nick mode)
    TOPIC ("TOPIC"),                // TOPIC channel [:newtopic]
    NAMES ("NAMES"),                // NAMES [channel{,channel}]
    LIST ("LIST"),                  // LIST [channel{,channel}]
    INVITE ("INVITE"),              // INVITE nick channel
    KICK ("KICK"),                  // KICK channel nick [:reason]

    /* sending messages */
    PRIVMSG ("MSG"),                // PRIVMSG (nick | channel) :text
    NOTICE ("NOTICE"),              // NOTICE nick :text

    /* user based queries */
    WHO ("WHO"),                    // WHO [name [o]]
    WHOIS ("WHOIS"),                // WHOIS nick
    WHOWAS ("WHOWAS"),              // WHOWAS nick

    /* miscellaneous messages */
    AWAY ("AWAY"),                  // AWAY [:reason]
    BACK ("BACK"),                  // BACK
    VERSION ("VERSION"),            // VERSION [server]
    STATS ("STATS"),                // STATS [query [server]]
    LINKS ("LINKS"),                // LINKS [[remote server] mask]
    TIME ("TIME"),                  // TIME [server]
    TRACE ("TRACE"),                // TRACE [server]
    ADMIN ("ADMIN"),                // ADMIN [server]
    INFO ("INFO"),                  // INFO [server]
    PONG ("PONG"),                  // PONG :text

    /* CTCP messages */
    /* CTCP messages are PRIVMSGs with the actual message (command and text) surrounded by \u0001 characters */
    CTCP ("CTCP"),                  // CTCP nick COMMAND (with COMMAND in { VERSION, SOURCE, USERINFO, CLIENTINFO, ERRMSG, PING, TIME })
    ACTION ("ME"),                  // ME message (converts to CTCP (nick|channel) \u0001ACTION message\u0001)

    /* convenient messages (not from the IRC RFC) */
    CLOSE ("CLOSE"),                // CLOSE (does a /part on the current channel or closes the private message window)
    QUERY ("QUERY"),                // QUERY nick (opens a private message window)

    /* unknown command */
    UNKNOWN ("");


    private final String mStr;

    /**
     * Enum constructor. Sets the internal string representation of each command.
     */
    UserCommand(String string) {
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
        return command.toUpperCase().equals(this.mStr);
    }

    /**
     * Returns true if the internal representation of this command starts with the given string.
     * The check is case insensitive.
     *
     * @param command a string to be compared to
     * @return true if the internal representation starts with the parameter, case insensitively
     */
    public boolean startsWithIgnoreCase(String command) {
        return this.mStr.startsWith(command.toUpperCase());
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

