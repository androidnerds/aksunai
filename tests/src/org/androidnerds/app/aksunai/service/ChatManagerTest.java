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
package org.androidnerds.app.aksunai.service;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

public class ChatManagerTest extends ServiceTestCase<ChatManager> {
	
	public ChatManagerTest() {
		super(ChatManager.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	private Intent getIntent() {
		return new Intent(getContext(), ChatManager.class);
	}
	
	public void testStartable() {
		startService(getIntent());
	}
	
	public void testBindable() {
		IBinder binder = bindService(getIntent());
	}
}
