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
package org.androidnerds.app.aksunai.net;

import android.content.Context;
import android.test.AndroidTestCase;

import org.androidnerds.app.aksunai.data.ServerDetail;
import org.androidnerds.app.aksunai.irc.Server;
import org.androidnerds.app.aksunai.net.ConnectionManager;

public class ConnectionManagerTest extends AndroidTestCase {

	public void testOpenConnection() {
		Context c = getContext();
		
		ConnectionManager cm = new ConnectionManager(c);
		ServerDetail sd = new ServerDetail(c, 1);
		
		Server s = cm.openConnection(sd);
			
	}
}
