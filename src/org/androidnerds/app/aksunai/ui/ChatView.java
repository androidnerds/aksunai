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

import android.content.Context;
import android.view.View;

import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.irc.Channel;
import org.androidnerds.app.aksunai.irc.Server;
import org.androidnerds.app.aksunai.irc.MessageList;

public class ChatView extends View {

	public ChatView(Context context, MessageList mlist, Server server) {
		super(context);
	}
	
}
