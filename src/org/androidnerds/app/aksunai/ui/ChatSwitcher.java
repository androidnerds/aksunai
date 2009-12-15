/*
 * Copyright (C) 2009 AndroidNerds.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.androidnerds.app.aksunai.net.Channel;
import org.androidnerds.app.aksunai.net.Server;

//TODO: fix the UI for the dialog. make it look nicer than it does.
public class ChatSwitcher extends Dialog {
    private ViewGroup mContainer;
    private Server mServer;
    private Context mCtx;

    public ChatSwitcher(Context c, Server s) {
        super(c, android.R.style.Theme_Dialog);
        mServer = s;
        mCtx = c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chat_switcher_dialog);
        mContainer = (ViewGroup) findViewById(R.id.pseudogallery);

        for (Channel c : mServer.channels.values()) {
            LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.chat_switcher_item, null);

            TextView tv = (TextView) v.findViewById(R.id.switcher_chat_title);
            tv.setText(c.name);

            ImageView iv = (ImageView) v.findViewById(R.id.avatar);
            
            if (c.type == Channel.TYPE.PM) {
            	iv.setImageResource(R.drawable.chat);
            } else if (c.type == Channel.TYPE.CHANNEL){
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

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            TextView chat = (TextView) v.findViewById(R.id.switcher_chat_title);
            mServer.setActiveChannel(chat.getText().toString());
            dismiss();
        }
    };
}
