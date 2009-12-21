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
import android.text.SpannableString;
import android.text.util.Linkify;
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
        setTranscriptMode(TRANSCRIPT_MODE_ALWAYS_SCROLL);
        setDividerHeight(0);
        
        this.setAdapter(new ChatAdapter(chatActivity));
	}

    private class ChatAdapter extends ArrayAdapter implements NewMessageListener {
        private ChatActivity mChatActivity;
        private LayoutInflater mInflater;
        private ChatManager mManager;
        private HashMap<String, Integer> mColorMap;

        public ChatAdapter(ChatActivity chatActivity) {
            super(chatActivity, R.layout.chat_row);

            this.mChatActivity = chatActivity;
            this.mInflater = chatActivity.getLayoutInflater();
            this.mManager = chatActivity.mManager;

            /* initialize the hashmap holding the colors */
            mColorMap = new HashMap<String, Integer>();
            mColorMap.put("nickname", mChatActivity.getResources().getColor(R.color.nickname));
            mColorMap.put("privmsg", mChatActivity.getResources().getColor(R.color.privmsg));
            mColorMap.put("ownmsg", mChatActivity.getResources().getColor(R.color.ownmsg));
            mColorMap.put("highlight", mChatActivity.getResources().getColor(R.color.highlight));
            mColorMap.put("action", mChatActivity.getResources().getColor(R.color.action));
            mColorMap.put("join", mChatActivity.getResources().getColor(R.color.join));
            mColorMap.put("part", mChatActivity.getResources().getColor(R.color.part));
            mColorMap.put("topic", mChatActivity.getResources().getColor(R.color.topic));

            this.mManager.mConnections.get(mServerName).setOnNewMessageListener(this);
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

            MessageList mlist = mManager.mConnections.get(mServerName).mMessageLists.get(mMessageListName);
            holder.setText(ChatMessageFormattedString(mlist.mMessages.get(pos)));

            Linkify.addLinks(holder, Linkify.ALL);
                        
            return convertView;
        }

        public int getCount() {
            MessageList mlist = mManager.mConnections.get(mServerName).mMessageLists.get(mMessageListName);
            return mlist.mMessages.size();
        }

        public void onNewMessage() {
        	mChatActivity.runOnUiThread(update);
        }

        private Runnable update = new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        };
        
        private SpannableString ChatMessageFormattedString(Message message) {
            SpannableString formattedMessage = new SpannableString("");
            if (message.mText != null) {
                formattedMessage = new SpannableString(message.mText);
            }
//           String chatMessage = sender.equals("") ? message : (sender + ": " + message);
//
//           if (sender.equals("")) {
//               /* PART or JOIN message */
//               formattedMessage.setSpan(new ForegroundColorSpan(colorMap.get("join")), 0, chatMessage.length(), 0);
//               formattedMessage.setSpan(new StyleSpan(Typeface.ITALIC), 0, chatMessage.length(), 0);
//           } else if (sender.equals(mServer.mNick)) {
//               /* own message */
//               formattedMessage.setSpan(new ForegroundColorSpan(colorMap.get("ownmsg")), 0, chatMessage.length(), 0);
//           } else if (message.toLowerCase().contains(mNick.toLowerCase())) { // case insensitive check
//               /* highlight */
//               formattedMessage.setSpan(new ForegroundColorSpan(colorMap.get("highlight")), 0, chatMessage.length(), 0);
//               formattedMessage.setSpan(new StyleSpan(Typeface.BOLD), 0, sender.length(), 0);
//           } else {
//               /* ACTION or PRIVMSG message */
//               formattedMessage.setSpan(new ForegroundColorSpan(colorMap.get("nickname")), 0, sender.length(), 0);
//           }

            return formattedMessage;
        }
    }
}
