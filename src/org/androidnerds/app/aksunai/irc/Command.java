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
 * Command is the enum used by {@link org.androidnerds.app.aksunai.irc.Message}.
 * <p>
 * It's used in switches, to style messages in windows, 
 * and the {@link org.androidnerds.app.aksunai.irc.Command#toString}
 * method is used to compare to the actual command received from the irc server.
 */
public enum Command {
    /* server initiated messages, no command, numeric */
    CONNECTED ("001"),              // :server 001 nick :Welcome to the freenode IRC Network OhanTest
    CHANNEL_TOPIC ("332"),          // :server 332 nick #channel :topic
    CHANNEL_TOPIC_SETTER ("333"),   // :server 333 nick #channel nick timestamp
    USERS ("353"),                  // :server 353 nick = #channel :nick{,nick}
    NO_NICK ("431"),                // :server 431 :No nickname given
    ERRONEUS_NICK ("432"),          // :server 432 * nick :Erroneous Nickname
    NICK_IN_USE ("433"),            // :server 433 * nick :Nickname is already in use
    NICK_COLLISION ("434"),         // :server 434 ???
    OTHER (""),

    /* user queries */
    NICK ("NICK"),                  // :nick!n=user@host NICK :newnick
    QUIT ("QUIT"),                  // :nick!n=user@host QUIT :reason

    /* channel operations */
    JOIN ("JOIN"),                  // :nick!n=user@host JOIN :#channel
    PART ("PART"),                  // :nick!n=user@host PART #channel :reason
    MODE ("MODE"),                  //
    TOPIC ("TOPIC"),                // :nick!n=user@host TOPIC #channel :topic
    NAMES ("NAMES"),                //
    LIST ("LIST"),                  //
    INVITE ("INVITE"),              // :nick!n=user@host INVITE othernick :#channel
    KICK ("KICK"),                  // :nick!n=user@host KICK #channel othernick :reason

    /* sending messages */
    PRIVMSG ("PRIVMSG"),            // :nick!n=user@host PRIVMSG #channel :test
    NOTICE ("NOTICE"),              // :nick!n=user@host NOTICE othernick :test

    /* miscellaneous messages */
    PING ("PING"),                  // PING :server

    /* CTCP messages */
    /* CTCP messages are PRIVMSGs with the actual message (command and text) surrounded by \u0001 characters */
    VERSION ("VERSION"),            // :nick!n=user@host PRIVMSG (nick|channel) :\u0001VERSION\u0001
    SOURCE ("SOURCE"),              // :nick!n=user@host PRIVMSG (nick|channel) :\u0001SOURCE\u0001
    USERINFO ("USERINFO"),          // :nick!n=user@host PRIVMSG (nick|channel) :\u0001USERINFO\u0001
    CLIENTINFO ("CLIENTINFO"),      // :nick!n=user@host PRIVMSG (nick|channel) :\u0001CLIENTINFO\u0001
    ERRMSG ("ERRMSG"),              // :nick!n=user@host PRIVMSG (nick|channel) :\u0001ERRMSG\u0001
    // following is commented as the PING command is already declared
    //PING ("PING"),                  // :nick!n=user@host PRIVMSG (nick|channel) :\u0001PING\u0001
    TIME ("TIME"),                  // :nick!n=user@host PRIVMSG (nick|channel) :\u0001TIME\u0001
    ACTION ("ACTION"),              // :nick!n=user@host PRIVMSG (nick|channel) :\u0001ACTION text\u0001

    /* unknown command */
    UNKNOWN ("UNKNOWN");


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
        return this.mStr.equals(command.toUpperCase());
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

