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
    private EditText entry;
	public ChatManager mManager;
	
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
            
            if (AppConstants.DEBUG) { Log.d(AppConstants.CHAT_TAG, "Connected to the service."); }
            runOnUiThread(updateChatViews);
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
                mManager.mConnections.get(chat.mServerName).userMessage(message, chat.mMessageListName);
            }
            entry.setText("");
        }
    }

    public void createChat(String serverName, String messageListName) {
        runOnUiThread(updateChatViews);
    }

    public Runnable updateChatViews = new Runnable() {
        public void run() {
            for (Server s : mManager.mConnections.values()) {
                ChatView chat;
                //create a view for the server itself
                if (getChatView(s.mName, s.mName) == null) {
                    chat = new ChatView(ChatActivity.this, s.mName, s.mName);

                    if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Create ChatView for Server: " + s.mName);
                    mFlipper.addView(chat);
                }

                //we need to setup a view for each channel/pm in this server
                for (MessageList mlist : s.mMessageLists.values()) {
                    if (getChatView(s.mName, mlist.mName) == null) {
                        if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Create ChatView for MessageList: " + mlist);
                        chat = new ChatView(ChatActivity.this, s.mName, mlist.mName);
                    
                        mFlipper.addView(chat, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
                    }
                }
            }
        }
    };

    public ChatView getChatView(String serverName, String messageListName) {
        for (int i = 0; i < mFlipper.getChildCount(); i++) {
            ChatView chat = (ChatView) mFlipper.getChildAt(i);
            if (chat.mServerName.equals(serverName) && chat.mMessageListName.equals(messageListName)) {
                return chat;
            }
        }
        return null;
    }
}
