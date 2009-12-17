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
import android.widget.ViewFlipper;

import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.service.ChatManager;
import org.androidnerds.app.aksunai.util.AppConstants;

public class ChatActivity extends Activity {

	private ViewFlipper mFlipper;
	private ChatManager mManager;
	
	@Override
	public void onCreate(Bundle appState) {
		super.onCreate(appState);
		
		setContentView(R.layout.chat_act);
		
		//grab the ViewFlipper from xml.
		mFlipper = (ViewFlipper) findViewById(R.id.chat_flipper);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (AppConstants.DEBUG){
			Log.d(AppConstants.CHAT_TAG, "Binding to the ChatManager service.");
		}
		
		bindService(new Intent(this, ChatManager.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			mManager = ((ChatManager.ChatBinder) service).getService();
			mFlipper.removeAllViews();
			
			if (AppConstants.DEBUG) {
				Log.d(AppConstants.CHAT_TAG, "Connected to the service.");
			}
			
			mManager.mConnections.size();
		}
		
		public void onServiceDisconnected(ComponentName name) {
			
		}
	};
}
