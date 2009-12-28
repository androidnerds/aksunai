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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.irc.Channel;
import org.androidnerds.app.aksunai.irc.MessageList;
import org.androidnerds.app.aksunai.irc.Server;

//TODO: fix the UI for the dialog. make it look nicer than it does.
public class ChatSwitcher extends Dialog {
    private ViewGroup mContainer;
    private Server mServer;
    private Context mCtx;
    private View.OnClickListener mClickListener;
    
    public ChatSwitcher(Context c, Server s, View.OnClickListener cl) {
        super(c, android.R.style.Theme_Dialog);
        mServer = s;
        mCtx = c;
        mClickListener = cl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chat_switcher_dialog);
        mContainer = (ViewGroup) findViewById(R.id.pseudogallery);
        
        for (MessageList c : mServer.mMessageLists.values()) {
            LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.chat_switcher_item, null);

            TextView tv = (TextView) v.findViewById(R.id.switcher_chat_title);
            tv.setText(c.mName);

            ImageView iv = (ImageView) v.findViewById(R.id.avatar);
            
            if (c.mType == MessageList.Type.PRIVATE) {
            	iv.setImageResource(R.drawable.chat);
            } else if (c.mType == MessageList.Type.CHANNEL){
            	iv.setImageResource(R.drawable.channel);
            } else {
            	iv.setImageResource(R.drawable.server);
        	}
            
            v.setOnClickListener(mClickListener);
            mContainer.addView(v);
        }

        mContainer.requestLayout();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
