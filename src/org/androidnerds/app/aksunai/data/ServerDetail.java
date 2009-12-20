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
package org.androidnerds.app.aksunai.data;

import android.content.Context;
import android.database.Cursor;

public class ServerDetail {

	public String mName;
	public String mUrl;
	public String mNick;
	public String mUser;
	public String mPass;
	public String mRealName;
	public int mPort;
	
	public ServerDetail(Context c, String name) {
		
		ServerDbAdapter db = new ServerDbAdapter(c);
		Cursor cur = db.getItem(name);
		
		if (cur.moveToNext()) {
			mName = cur.getString(1);
			mUrl = cur.getString(2);
			mUser = cur.getString(3);
			mNick = cur.getString(4);
			mPass = cur.getString(5);
			mPort = cur.getInt(6);
			mRealName = cur.getString(7);
		}
		
		cur.close();
		db.release();
	}
	
	public String toString() {
		return mName + " " + mUrl;
	}
}
