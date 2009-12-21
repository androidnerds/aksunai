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

public class ChatView extends ListView {
    private ChatAdapter mAdapter;
    public Server mServer;
    public MessageList mMessageList;

	public ChatView(Context context, MessageList mlist, Server server) {
		super(context);

        this.mServer = server;
        this.mMessageList = mlist;

        mAdapter = new ChatAdapter(context, mlist, server);
        this.setAdapter(mAdapter);
	}

    public void updateChat() {
        mAdapter.notifyDataSetChanged();
    }

    private class ChatAdapter extends ArrayAdapter {
        private Context mCtx;
        private LayoutInflater mInflater;
        private MessageList mMessageList;
        private Server mServer;

        private HashMap<String, Integer> colorMap;

        public ChatAdapter(Context c, MessageList mlist, Server server) {
            super(c, R.layout.chat_row);
            this.mCtx = c;
            this.mMessageList = mlist;
            this.mServer = server;

            mInflater = LayoutInflater.from(mCtx);

            /* initialize the hashmap holding the colors */
            colorMap = new HashMap<String, Integer>();
            colorMap.put("nickname", mCtx.getResources().getColor(R.color.nickname));
            colorMap.put("privmsg", mCtx.getResources().getColor(R.color.privmsg));
            colorMap.put("ownmsg", mCtx.getResources().getColor(R.color.ownmsg));
            colorMap.put("highlight", mCtx.getResources().getColor(R.color.highlight));
            colorMap.put("action", mCtx.getResources().getColor(R.color.action));
            colorMap.put("join", mCtx.getResources().getColor(R.color.join));
            colorMap.put("part", mCtx.getResources().getColor(R.color.part));
            colorMap.put("topic", mCtx.getResources().getColor(R.color.topic));
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

            holder.setText(ChatMessageFormattedString(mMessageList.mMessages.get(pos)));

            Linkify.addLinks(holder, Linkify.ALL);
                        
            return convertView;
        }

        private SpannableString ChatMessageFormattedString(Message message) {
             SpannableString formattedMessage = new SpannableString(message.mText);
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
