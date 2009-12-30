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
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    private ChatSwitcher mChatSwitcher;
    private NotificationManager mNotificationManager;
    
    /* gesture listener */
    private static final int SWIPE_MIN_DISTANCE = 100;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
    private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
    private Animation slideRightOut;
	
	@Override
	public void onCreate(Bundle appState) {
		super.onCreate(appState);
		
		setContentView(R.layout.chat);

        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
		
		//grab the ViewFlipper from xml.
		mFlipper = (ViewFlipper) findViewById(R.id.chat_flipper);
        
        entry = (EditText) findViewById(R.id.ircedit);
        entry.setSingleLine();
        entry.setOnKeyListener(mKeyListener);

        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(mClickListener);

        /* fling detection */
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        mFlipper.setOnTouchListener(gestureListener);
        
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

	@Override
	public void onStart() {
		super.onStart();
		
		if (AppConstants.DEBUG) {
			Log.d(AppConstants.CHAT_TAG, "Binding to the ChatManager service.");
		}
		
        bindService(new Intent(this, ChatManager.class), mConnection, Context.BIND_AUTO_CREATE);
		
	}
	
    @Override
    public void onPause() {
        super.onPause();        
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        unbindService(mConnection);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        mNotificationManager.cancel(R.string.notify_new_private_chat);
        mNotificationManager.cancel(R.string.notify_nick_in_chat);

        Intent i = getIntent();
        
        if (i.hasExtra("server") && i.hasExtra("chat")) {
            //receiving notification of a new chat.
            Log.d(AppConstants.CHAT_TAG, "New notification has been received.");
        }
    }
    
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			mManager = ((ChatManager.ChatBinder) service).getService();
			
			//once the service is available see if we need a new connection.
			if (getIntent().hasExtra("id")) {
				Bundle extras = getIntent().getExtras();
				ServerDetail details = new ServerDetail(ChatActivity.this, extras.getLong("id"));
				
                boolean found = false;
                for (Server s : mManager.mConnections.values()) {
                    if (s.mName.equals(details.mName)) {
                        found = true;
                    }
                }

				if (!found) {
                    Log.d(AppConstants.CHAT_TAG, "need to establish a connection.");
					mManager.openServerConnection(ChatActivity.this, details);
				}
			}
			
            if (AppConstants.DEBUG) { Log.d(AppConstants.CHAT_TAG, "Connected to the service."); }
            runOnUiThread(updateChatViews);
		}
		
		public void onServiceDisconnected(ComponentName name) {
            if (AppConstants.DEBUG) { Log.d(AppConstants.CHAT_TAG, "Disconnected from the service."); }
            finish();
		}
	};
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ChatView chat = (ChatView) mFlipper.getCurrentView();
        MessageList mlist = mManager.mConnections.get(chat.mServerName).mMessageLists.get(chat.mMessageListName);
        menu.findItem(R.id.menu_show_user_list).setEnabled(mlist.mType == MessageList.Type.CHANNEL);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: propper menu handling
        switch (item.getItemId()) {
        case R.id.menu_close_window:
            sendUserMessage("/close");
            break;
        case R.id.menu_open_windows:
            showChatDialog();
            break;
        case R.id.menu_disconnect:
            sendUserMessage("/quit");
            //ConnectionService.disconnectFromServer(ConnectionService.activeServer);
            break;
        case R.id.menu_show_user_list:
            ChatView chat = (ChatView) mFlipper.getCurrentView();
            Intent intent = new Intent(this , UserList.class);
            
            intent.putExtra("server", chat.mServerName);
            intent.putExtra("channel", chat.mMessageListName);
            
            startActivity(intent);
            break;
        }
        return true;
    }
    
    private void showChatDialog() {
        // TODO: proper ChatSwitch display
        ChatView chat = (ChatView) mFlipper.getCurrentView();
        mChatSwitcher = new ChatSwitcher(this, mManager.mConnections.get(chat.mServerName), mSwitcherListener);
        mChatSwitcher.show();
    }

    private OnKeyListener mKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int i, KeyEvent k) {
            if (k.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                sendUserInput();
                return false;
            }

            return false;
        }
    };

    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            sendUserInput();
        }
    };

    private OnClickListener mSwitcherListener = new OnClickListener() {
        public void onClick(View v) {
            ChatView cv = (ChatView) mFlipper.getCurrentView();
            TextView chat = (TextView) v.findViewById(R.id.switcher_chat_title);
            ChatView c = getChatView(cv.mServerName, chat.getText().toString());
            int i = mFlipper.indexOfChild(c);
            mFlipper.setDisplayedChild(i);
            mChatSwitcher.dismiss();
            mChatSwitcher = null;
        }
    };
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { /* right to left swipe */
                    if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Fling left");
                	mFlipper.setInAnimation(slideLeftIn);
                    mFlipper.setOutAnimation(slideLeftOut);
                	mFlipper.showNext();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { /* left to right swipe */
                    if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Fling right");
                	mFlipper.setInAnimation(slideRightIn);
                    mFlipper.setOutAnimation(slideRightOut);
                	mFlipper.showPrevious();
                }
            } catch (Exception e) {}
            return false;
        }
    }

    private void sendUserMessage(String message) {
        ChatView chat = (ChatView) mFlipper.getCurrentView();
        if (chat != null) {
            if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Sending user message: " + message);
            mManager.mConnections.get(chat.mServerName).userMessage(message, chat.mMessageListName);
        }
    }

    private void sendUserInput() {
        String message = entry.getText().toString();
        
        if (!message.equals("")) { // don't send empty messages
            sendUserMessage(message);
        }
        entry.setText("");
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
                    chat.setOnTouchListener(gestureListener);

                    if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Create ChatView for Server: " + s.mName);
                    mFlipper.addView(chat);
                }

                //we need to setup a view for each channel/pm in this server
                for (MessageList mlist : s.mMessageLists.values()) {
                    if (getChatView(s.mName, mlist.mName) == null) {
                        if (AppConstants.DEBUG) Log.d(AppConstants.CHAT_TAG, "Create ChatView for MessageList: " + mlist);
                        chat = new ChatView(ChatActivity.this, s.mName, mlist.mName);
                        chat.setOnTouchListener(gestureListener);
                    
                        // TODO: add the view in the correct place to respect order (by server, and then alphabetically?): added in front for now
                        mFlipper.addView(chat, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
                        mFlipper.setDisplayedChild(0);
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
