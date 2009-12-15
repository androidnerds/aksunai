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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidnerds.app.aksunai.MyConfig;
import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.net.Channel;
import org.androidnerds.app.aksunai.net.ConnectionService;
import org.androidnerds.app.aksunai.net.Server;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Vector;

/* The chat activity only handles one server at a time. */
@SuppressWarnings(value = { "unchecked" })
public class Chat extends ListActivity {

    private ChatAdapter mAdapter;
    private Server mServer;
    private EditText entry;
    private GestureDetector mGestureDetector;

    public static final int STATE_WINDOW_CLOSED = 0;
    public static final int STATE_WINDOW_OPEN = 1;

    private static final int OPEN_WINDOWS = Menu.FIRST;
    private static final int CLOSE_WINDOW = Menu.FIRST + 1;
    private static final int DISCONNECT = Menu.FIRST + 2;
    private static final int SHOW_USER_LIST = Menu.FIRST + 3;

    private static final int MINIMUM_GESTURE_DISTANCE = 25;
    private ProgressDialog pd;
    public AlertDialog mInfoDialog;

    @Override
    public void onCreate(Bundle appState) {
        super.onCreate(appState);

        setContentView(R.layout.chat);

        //when entering the Chat activity we get passed what server should be active.
        Bundle extras = getIntent().getExtras();
        if (appState != null) {
            extras = appState;
        }

        String name = extras.getString("name");
        mServer = ConnectionService.connections.get(name);
        ConnectionService.activeServer = name;

        if (extras.containsKey("channel")) {
            String channel = extras.getString("channel");
            mServer.activeChannel = mServer.channels.get(channel);
        }

        mAdapter = new ChatAdapter(this, mServer.mNick);
        setListAdapter(mAdapter);

        getListView().setStackFromBottom(true);
        getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        getListView().setDividerHeight(0);

        entry = (EditText)findViewById(R.id.ircedit);
        entry.setSingleLine();
        entry.setOnKeyListener(mKeyListener);

        setupChannel();

        mServer.setHandler(mHandler);
        ConnectionService.STATE_CHAT_WINDOW = Chat.STATE_WINDOW_OPEN;

        Button btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(mClickListener);

        getListView().setOnTouchListener(mScreenTouch);

        mGestureDetector = new GestureDetector(this, mGestureListener);
        mGestureDetector.toString();

        if (mServer.state != Server.STATE_CONNECTED) {
            pd = ProgressDialog.show(this, getString(R.string.connecting), getString(R.string.connecting_to) + " " + mServer.mName, true, true);
        }

        registerForContextMenu(getListView());
    }

    @Override
    public void onPause() {
        super.onPause();
        ConnectionService.STATE_CHAT_WINDOW = Chat.STATE_WINDOW_CLOSED;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();

        if (extras.containsKey("channel")) {
            String channel = extras.getString("channel");
            mServer.activeChannel = mServer.channels.get(channel);
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(R.string.incoming_message);

        ConnectionService.STATE_CHAT_WINDOW = Chat.STATE_WINDOW_OPEN;

        //make a call to setupchannel to make sure the active channel is visible.
        setupChannel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, OPEN_WINDOWS, 1, R.string.chats).setIcon(R.drawable.ic_menu_chat_dashboard);
        menu.add(0, CLOSE_WINDOW, 2, R.string.close).setIcon(R.drawable.ic_menu_end_conversation);
        menu.add(0, DISCONNECT, 3, R.string.disconnect).setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        if (mServer.activeChannel == null) {
            menu.add(0, SHOW_USER_LIST, 4, R.string.show_users).setIcon(R.drawable.ic_menu_friend_list).setEnabled(false);
        } else {
            menu.add(0, SHOW_USER_LIST, 4, R.string.show_users).setIcon(R.drawable.ic_menu_friend_list);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CLOSE_WINDOW:
            mServer.sendMessage("/close");
            return true;

        case OPEN_WINDOWS:
            showChatDialog();
            break;

        case DISCONNECT:
            ConnectionService.disconnectFromServer(ConnectionService.activeServer);
            break;

        case SHOW_USER_LIST:
            startActivity(new Intent(Chat.this, UserList.class));
            break;
        }

        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo i) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) i;

        if (mAdapter.mSenders.get(info.position).equals(""))
            return;

        menu.setHeaderTitle(getString(R.string.options) + " " + mAdapter.mSenders.elementAt(info.position));
        menu.add(getString(R.string.private_message));
        menu.add(getString(R.string.info));
        menu.add(getString(R.string.ignore));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getTitle().equals(getString(R.string.private_message))) {
            mServer.openNewPM(mAdapter.mSenders.get(info.position));
        }

        if (item.getTitle().equals(getString(R.string.info))) {
            mServer.sendMessage("/whois " + mAdapter.mSenders.get(info.position));
        }

        if (MyConfig.DEBUG) Log.d("Aksunai", "The sender is..." + mAdapter.mSenders.get(info.position));
        return false;
    }

    private void showChatDialog() {
        ChatSwitcher c = new ChatSwitcher(this, mServer);
        c.show();
    }

    private void setupChannel() {
        if (mServer.activeChannel == null) {
            setTitle(getString(R.string.app_name) + " - " + mServer.mName);
        } else {
            setTitle(getString(R.string.app_name) + " - " + mServer.activeChannel.name);
        }

        if (mServer.activeChannel != null) {
            updateView(mServer.activeChannel.name);
        }
    }

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
        
        if (msg.startsWith("/topic")) {
        	displayTopic();
        	entry.setText("");
        	return;
        }
        
        if (!msg.equals("")) { // don't send empty messages
            mServer.sendMessage(entry.getText().toString());
            entry.setText("");
        }
    }

    GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int dx = (int)(e2.getX() - e1.getX());

            if (Math.abs(dx) > MINIMUM_GESTURE_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX > 0) {
                    forwardChannel();
                } else {
                    backwardChannel();
                }

                return true;
            }
            return false;
        }
    };

    private final OnTouchListener mScreenTouch = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    };

    public void forwardChannel() {
        //move to the next channel.
        boolean grabNext = false;
        for (Channel c : mServer.channels.values()) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "grabNext is: " + grabNext);
            if (grabNext) {
                mServer.activeChannel = c;
                if (MyConfig.DEBUG) Log.d("Aksunai", "Setting up next channel");
                setupChannel();
                break;
            }

            if (MyConfig.DEBUG) Log.d("Aksunai", "Channel: " + c.name + " and activeChannel: " + mServer.activeChannel.name);
            if (c.name.equals(mServer.activeChannel.name)) {
                grabNext = true;
            }
        }
    }

    public void backwardChannel() {
        //move back to the previous channel.
        boolean grabPrevious = false;
        Channel prev = null;

        for (Channel c : mServer.channels.values()) {
            if (c == null || mServer.activeChannel == null) {
                break;
            }

            if (prev != null) {
                if (MyConfig.DEBUG) Log.d("Aksunai", "grabPrevious is: " + grabPrevious + " and prev: " + prev.name);
            }

            if (c.name.equals(mServer.activeChannel.name)) {
                grabPrevious = true;
            }

            if (grabPrevious && prev != null) {
                mServer.activeChannel = prev;
                if (MyConfig.DEBUG) Log.d("Aksunai", "Setting up last channel");
                setupChannel();
                break;
            }

            if (MyConfig.DEBUG) Log.d("Aksunai", "Channel: " + c.name + " and activeChannel: " + mServer.activeChannel.name);

            prev = c;
        }
    }

    public void updateView(String chan) {
        Channel channel = mServer.activeChannel;

        mAdapter.clear();

        for (int i = 0; i < channel.conversation.size(); i++) {
            mAdapter.loadData(channel.conversation.get(i));
        }
    }

    public void hideProgressDialog() {
        pd.dismiss();
    }

    public void displayInfo(Hashtable<String, String> info) {
        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.info_nickname) + " ");
        message.append(info.get("nick") + "\n");
        message.append(getString(R.string.info_username) + " ");
        message.append(info.get("username") + "\n");
        message.append(getString(R.string.info_realname) + " ");
        message.append(info.get("realname") + "\n");
        message.append(getString(R.string.info_server) + " ");
        message.append(info.get("server") + "\n");
        message.append(getString(R.string.info_channels) + " ");
        message.append(info.get("channels") + "\n");

        mInfoDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.info_title_part) + " " + info.get("nick")).setMessage(message.toString())
        .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /* User clicked Cancel so do some stuff */
                Chat.this.mInfoDialog.dismiss();
            }
        }).show();
    }

    public void displayTopic() {
    	mInfoDialog = new AlertDialog.Builder(this).setTitle(mServer.activeChannel.name)
    	.setMessage(mServer.activeChannel.topic)
    	.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			Chat.this.mInfoDialog.dismiss();
    		}
    	}).show();
    }
    
    private class ChatAdapter extends BaseAdapter {
        private Vector<String> mSenders;
        private Vector<String> mMessages;
        private Context mCtx;
        private LayoutInflater mInflater;
        private String mNick;

        private HashMap<String, Integer> colorMap;

        public ChatAdapter(Context c, String nick) {
            mCtx = c;
            mSenders = new Vector<String>();
            mMessages = new Vector<String>();

            mInflater = LayoutInflater.from(mCtx);
            mNick = nick;

            /* initialize the hashmap holding the colors */
            colorMap = new HashMap<String, Integer>();
            colorMap.put("nickname", mCtx.getResources().getColor(R.color.nickname));
            colorMap.put("privmsg", mCtx.getResources().getColor(R.color.privmsg));
            colorMap.put("ownmsg", mCtx.getResources().getColor(R.color.ownmsg));
            colorMap.put("highlight", mCtx.getResources().getColor(R.color.highlight));
            colorMap.put("action", mCtx.getResources().getColor(R.color.action));
            colorMap.put("join", mCtx.getResources().getColor(R.color.join));
            colorMap.put("part", mCtx.getResources().getColor(R.color.part));
        }

        public int getCount() {
            return mSenders.size();
        }

        public Object getItem(int pos) {
            return mMessages.elementAt(pos);
        }

        public long getItemId(int pos) {
            return pos;
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

            holder.setText(ChatMessageFormattedString(mSenders.elementAt(pos), mMessages.elementAt(pos)));

            Linkify.addLinks(holder, Linkify.ALL);
                        
            return convertView;
        }

        public void loadData(String raw) {
            //remove the op prefix.
            if (raw.startsWith("@")) {
                raw = raw.substring(1);
            }

            if (raw.substring(raw.indexOf(" ") + 1).trim().startsWith("ACTION")) {
                mSenders.add("***");
                mMessages.add(raw.substring(0, raw.indexOf(" ")).trim() + " " + raw.substring(raw.indexOf("ACTION") + 6).trim());
            } else if (raw.substring(raw.indexOf(" ") + 1).trim().startsWith("JOIN")) {
                mSenders.add("");
                mMessages.add(raw.substring(raw.indexOf("JOIN") + 4).trim());
            } else if (raw.substring(raw.indexOf(" ") + 1).trim().startsWith("PART")) {
                mSenders.add("");
                mMessages.add(raw.substring(raw.indexOf("PART") + 4).trim());
            } else if (raw.substring(0, raw.indexOf(" ")).contains("topic")) {
            	mSenders.add("Topic");
            	mMessages.add(raw.substring(raw.indexOf(" ") + 1).trim());
            } else {
                mSenders.add(raw.substring(0, raw.indexOf(" ")).trim());
                mMessages.add(raw.substring(raw.indexOf(" ") + 1).trim());
            }

            notifyDataSetChanged();
        }

        public void clear() {
            mSenders.clear();
            mMessages.clear();

            notifyDataSetChanged();
        }

        private SpannableString ChatMessageFormattedString(String sender, String message) {
            String chatMessage = sender.equals("") ? message : (sender + ": " + message);
            SpannableString formattedMessage = new SpannableString(chatMessage);

            if (sender.equals("")) {
                /* PART or JOIN message */
                formattedMessage.setSpan(new ForegroundColorSpan(colorMap.get("join")), 0, chatMessage.length(), 0);
                formattedMessage.setSpan(new StyleSpan(Typeface.ITALIC), 0, chatMessage.length(), 0);
            } else if (sender.equals("me")) {
                /* own message */
                formattedMessage.setSpan(new ForegroundColorSpan(colorMap.get("ownmsg")), 0, chatMessage.length(), 0);
            } else if (message.toLowerCase().contains(mNick.toLowerCase())) { // case insensitive check
                /* highlight */
                formattedMessage.setSpan(new ForegroundColorSpan(colorMap.get("highlight")), 0, chatMessage.length(), 0);
                formattedMessage.setSpan(new StyleSpan(Typeface.BOLD), 0, sender.length(), 0);
            } else {
                /* ACTION or PRIVMSG message */
                formattedMessage.setSpan(new ForegroundColorSpan(colorMap.get("nickname")), 0, sender.length(), 0);
            }

            return formattedMessage;
        }
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case Server.STATE_NICK_IN_USE:
                //TODO: prompt the user to modify their nick.
                break;
            case Server.STATE_NICK_BAD:
                //TODO: there is an error with the nick, user needs to modify.
                break;
            case Server.STATE_CONNECTED:
                hideProgressDialog();
                break;
            case Server.STATE_DISCONNECTED:
                finish();
                break;
            case Server.STATE_LOGGED_IN:
                Toast.makeText(Chat.this, getString(R.string.authenticated), Toast.LENGTH_LONG).show();
                break;
            case Server.MSG_UPDATE_CHANNEL:
                updateView((String)msg.obj);
                break;
            case Server.MSG_NEW_WINDOW:
                setupChannel();
                break;
            case Server.MSG_INFO_REQUEST:
                displayInfo((Hashtable<String, String>)msg.obj);
                break;
            case Server.MSG_UNKNOWN_COMMAND:
                Toast.makeText(Chat.this, getString(R.string.unknown_command) + " " + msg.obj, Toast.LENGTH_LONG).show();
                break;
            case Server.MSG_NO_CHANNEL:
                Toast.makeText(Chat.this, getString(R.string.no_channel), Toast.LENGTH_LONG).show();
            }
        }
    };
}
