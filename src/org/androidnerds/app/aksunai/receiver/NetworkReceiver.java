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
package org.androidnerds.app.aksunai.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;

import org.androidnerds.app.aksunai.service.ChatManager;

public class NetworkReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {		
		if (ChatManager.running) {
			NetworkInfo n = (NetworkInfo) i.getExtras().get("NetworkInfo");
			Intent manager = new Intent(c, ChatManager.class);
		
			if (n.isConnected()) {
				manager.putExtra("connected", true);
			} else {
				manager.putExtra("connected", false);
			}
			
			c.startService(manager);
		}
	}
}