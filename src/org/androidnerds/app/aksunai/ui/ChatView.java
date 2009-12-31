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
package org.androidnerds.app.aksunai.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.irc.Channel;
import org.androidnerds.app.aksunai.irc.Command;
import org.androidnerds.app.aksunai.irc.Message;
import org.androidnerds.app.aksunai.irc.Server;
import org.androidnerds.app.aksunai.irc.MessageList;
import org.androidnerds.app.aksunai.irc.MessageList.NewMessageListener;
import org.androidnerds.app.aksunai.service.ChatManager;
import org.androidnerds.app.aksunai.util.AppConstants;

public class ChatView extends ListView {
    public String mServerName;
    public String mMessageListName;

	public ChatView(ChatActivity chatActivity, String serverName, String messageListName) {
		super(chatActivity);

        this.mServerName = serverName;
        this.mMessageListName = messageListName;
        		
        setStackFromBottom(true);
        setTranscriptMode(TRANSCRIPT_MODE_ALWAYS_SCROLL); // TODO: check why this doesn't work
        setDividerHeight(0);
        
        this.setAdapter(new ChatAdapter(chatActivity, serverName, messageListName));
	}
	
    private static class ChatAdapter extends ArrayAdapter implements NewMessageListener {
        private ChatActivity mChatActivity;
        private LayoutInflater mInflater;
        private ChatManager mManager;
        private HashMap<String, Integer> mColorMap;
		private String serverName;
		private String messageListName;
		
        public ChatAdapter(ChatActivity chatActivity, String serverName, String messageListName) {
            super(chatActivity, R.layout.chat_row);

            this.mChatActivity = chatActivity;
            this.mInflater = chatActivity.getLayoutInflater();
            this.mManager = chatActivity.mManager;
			this.serverName = serverName;
			this.messageListName = messageListName;
			
            /* initialize the hashmap holding the colors */
            mColorMap = new HashMap<String, Integer>();
            mColorMap.put("sender", mChatActivity.getResources().getColor(R.color.sender));
            mColorMap.put("privmsg", mChatActivity.getResources().getColor(R.color.privmsg));
            mColorMap.put("ownmsg", mChatActivity.getResources().getColor(R.color.ownmsg));
            mColorMap.put("highlight", mChatActivity.getResources().getColor(R.color.highlight));
            mColorMap.put("action", mChatActivity.getResources().getColor(R.color.action));
            mColorMap.put("join", mChatActivity.getResources().getColor(R.color.join));
            mColorMap.put("part", mChatActivity.getResources().getColor(R.color.part));
            mColorMap.put("topic", mChatActivity.getResources().getColor(R.color.topic));
            mColorMap.put("nick", mChatActivity.getResources().getColor(R.color.nick));

            this.mManager.mConnections.get(serverName).mMessageLists.get(messageListName).setOnNewMessageListener(this);
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            TextView holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.chat_row, parent, false);

                holder = (TextView) convertView.findViewById(R.id.message);
                convertView.setTag(holder);
            } else {
                holder = (TextView) convertView.getTag();
            }

            MessageList mlist = mManager.mConnections.get(serverName).mMessageLists.get(messageListName);
            holder.setText(ChatMessageFormattedString(mlist.mMessages.get(pos)));

            Linkify.addLinks(holder, Linkify.ALL);
                        
            return convertView;
        }

        public int getCount() {
            MessageList mlist = mManager.mConnections.get(serverName).mMessageLists.get(messageListName);
            return mlist.mMessages.size();
        }

        public void onNewMessage(String message, String server, String list) {
        	mChatActivity.runOnUiThread(update);
        }

        private Runnable update = new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        };
        
        private SpannableStringBuilder ChatMessageFormattedString(Message message) {
            String nick = mManager.mConnections.get(serverName).mNick;
            nick = (nick != null) ? nick : "!notset!";

            SpannableStringBuilder formatted = new SpannableStringBuilder();
            SpannableStringBuilder sender = new SpannableStringBuilder((message.mSender != null) ? message.mSender : "");
            SpannableStringBuilder text = new SpannableStringBuilder((message.mText != null) ? message.mText : "");

            if (message.mCommand == Command.NICK) {
                formatted.append(sender).append(" " + mChatActivity.getString(R.string.nick_change) + " ").append(text);
                setColor(formatted, "nick");
			} else if (message.mCommand == Command.ACTION) { /* CTCP ACTION message */
				formatted.append("* " + sender + " " + text);
				setColor(formatted, "action");
            } else if (sender.toString().toLowerCase().equals(nick.toLowerCase())) { /* own message */
                if (!message.mParameters[0].toLowerCase().equals(messageListName.toLowerCase())) { /* private message or notice to somebody else */
                    formatted.append(">" + message.mParameters[0] + "< ").append(text);
                } else {
                    formatted.append(sender).append(": ").append(text);
                }
                setColor(formatted, "ownmsg");
            } else if (message.mCommand == Command.NOTICE && !sender.toString().equals("")) {
                formatted.append("-").append(sender).append("- ").append(text);
                setItalic(setColor(formatted, "highlight"));
            } else if (message.mCommand == Command.CHANNEL_TOPIC || message.mCommand == Command.CHANNEL_TOPIC_SETTER) {
                formatted.append(setColor(text, "topic"));
            } else if (message.mCommand == Command.JOIN) {
                formatted.append(setItalic(setColor(sender.append(" " + mChatActivity.getString(R.string.has_joined) + " ").append(text), "join")));
            } else if (message.mCommand == Command.PART) {
                formatted.append(sender).append(" " + mChatActivity.getString(R.string.has_left) + "(").append(text).append(")");
                setItalic(setColor(formatted, "part"));
            } else if (message.mCommand == Command.QUIT) {
                formatted.append(sender).append(" " + mChatActivity.getString(R.string.has_quit) + "(").append(text).append(")");
                setItalic(setColor(formatted, "part"));
            } else if (sender.toString().equals("")) { /* server notice or message */
                formatted.append("* ").append(text);
            } else if (text.toString().toLowerCase().contains(nick.toLowerCase())) { /* highlight */
                formatted.append(sender).append(": ").append(text);
                setColor(formatted, "highlight");
            } else { /* standard message */
                formatted.append(setColor(sender, "sender")).append(": ").append(text);
            }

            return formatted;
        }

        private SpannableStringBuilder setColor(SpannableStringBuilder msg, String color) {
            msg.setSpan(new ForegroundColorSpan(mColorMap.get(color)), 0, msg.length(), 0);
            return msg;
        }

        private SpannableStringBuilder setBold(SpannableStringBuilder msg) {
            msg.setSpan(new StyleSpan(Typeface.BOLD), 0, msg.length(), 0);
            return msg;
        }

        private SpannableStringBuilder setItalic(SpannableStringBuilder msg) {
            msg.setSpan(new StyleSpan(Typeface.ITALIC), 0, msg.length(), 0);
            return msg;
        }
    }

	public String toString() {
		return "ChatView :: Server: " + mServerName + "; MessageList: " + mMessageListName;
	}
}
