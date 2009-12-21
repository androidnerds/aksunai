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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.data.ServerDetail;
import org.androidnerds.app.aksunai.irc.Channel;
import org.androidnerds.app.aksunai.irc.Server;
import org.androidnerds.app.aksunai.irc.Message;
import org.androidnerds.app.aksunai.irc.MessageList;
import org.androidnerds.app.aksunai.service.ChatManager;
import org.androidnerds.app.aksunai.util.AppConstants;

public class ChatActivity extends Activity {

	private ViewFlipper mFlipper;
	private ChatManager mManager;
    private EditText entry;
	
	@Override
	public void onCreate(Bundle appState) {
		super.onCreate(appState);
		
		setContentView(R.layout.chat);
		
		//grab the ViewFlipper from xml.
		mFlipper = (ViewFlipper) findViewById(R.id.chat_flipper);
        
        entry = (EditText) findViewById(R.id.ircedit);
        entry.setSingleLine();
        entry.setOnKeyListener(mKeyListener);

        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(mClickListener);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (AppConstants.DEBUG) {
			Log.d(AppConstants.CHAT_TAG, "Binding to the ChatManager service.");
		}
		
		bindService(new Intent(this, ChatManager.class), mConnection, Context.BIND_AUTO_CREATE);
		
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			mManager = ((ChatManager.ChatBinder) service).getService();
			
			//once the service is available see if we need a new connection.
			if (getIntent().hasExtra("id")) {
				Bundle extras = getIntent().getExtras();
				ServerDetail details = new ServerDetail(ChatActivity.this, extras.getLong("id"));
				
				if (mManager != null) {
					mManager.openServerConnection(ChatActivity.this, details);
				}
			}
			
			mFlipper.removeAllViews();
			
			if (AppConstants.DEBUG) {
				Log.d(AppConstants.CHAT_TAG, "Connected to the service.");
			}
			
			mManager.mConnections.size();
			
			//we need to setup a view for each channel/pm in each server.
			for (Server s : mManager.mConnections) {
				//create a view for the server itself
                ChatView chat = new ChatView(ChatActivity.this, s, s);

                if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Create ChatView for Server: " + s);
                mFlipper.addView(chat);
				for (MessageList mlist : s.mMessageLists.values()) {
                    if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Create ChatView for MessageList: " + mlist);
					chat = new ChatView(ChatActivity.this, mlist, s);
					chat.setId(R.id.chat_flipper);
					
					mFlipper.addView(chat, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
				}
			}
		}
		
		public void onServiceDisconnected(ComponentName name) {
			
		}
	};

    private OnKeyListener mKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int i, KeyEvent k) {
            if (k.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                sendUserMessage();
                return false;
            }

            return false;
        }
    };

    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            sendUserMessage();
        }
    };

    private void sendUserMessage() {
        String msg = entry.getText().toString();
        
        if (!msg.equals("")) { // don't send empty messages
            ChatView chat = (ChatView) mFlipper.getCurrentView();
            if (chat != null) {
                String message = entry.getText().toString();
                if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Sending user message: " + message);
                chat.mServer.userMessage(message, chat.mMessageList);
            }
            entry.setText("");
        }
    }

    public void createChat(Server server, MessageList mlist) {
    }

    public void updateChat(Server server, Message message, MessageList mlist) {
        ChatView chat = (ChatView) mFlipper.getCurrentView();
        if (chat.mServer == server && chat.mMessageList == mlist) { // only update if it's the current view
            chat.updateChat();
        }
    }
}
