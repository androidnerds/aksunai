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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

import java.util.Hashtable;

import org.androidnerds.app.aksunai.MyConfig;
import org.androidnerds.app.aksunai.ui.Chat;

/* Service only needs to be running if a connection is open.
 * The last connection terminated will destroy the service.
 * Also, if the user wishes to disconnect from the entire application the service will be terminated.
 * If the service is not running, its started right before the first servers calls NewConnection()
 *
 * TODO: ConnectionService needs to have a method deal with network connectivity interrupts.
 */
public class ConnectionService extends Service {

    public static Hashtable<String, Server> connections;
    public static String activeServer;

    public static boolean IS_RUNNING = false;
    public static boolean IS_CONNECTED = false;
    public static int STATE_CHAT_WINDOW;

    @Override
    public void onCreate() {
        //onCreate method should only initialize.
        connections = new Hashtable<String, Server>();
    }

    @Override public void onStart(Intent intent, int startId) {
        //this is where data can assigned to variables.
        IS_RUNNING = true;
        ConnectionService.STATE_CHAT_WINDOW = Chat.STATE_WINDOW_CLOSED;

        if (MyConfig.DEBUG) Log.d("Aksunai", "IS_RUNNING should now be set to true");

        Bundle extras = intent.getExtras();

        //before making any network connection make sure you have it.
        if (determineNetworkState()) {
            NewServerConnection(this, extras.getLong("id"), extras.getString("name"));
            activeServer = extras.getString("name");
        } else {
            IS_RUNNING = false;
            IS_CONNECTED = false;
            this.stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        //close out all connections to all servers.
    }

    public IBinder onBind(Intent intent) {
        return getBinder();
    }

    public IBinder getBinder() {
        return mBinder;
    }

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            try {
                return super.onTransact(code, data, reply, flags);
            } catch (Exception e) {
                return false;
            }
        }
    };

    private boolean determineNetworkState() {
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        } else {
            return true;
        }
    }

    public static void disconnectFromServer(String server) {
        Server s = connections.get(server);
        s.closeConnection();
        connections.remove(server);
    }

    /* this method is for starting a new connection. */
    public static void NewServerConnection(Context c, long id, String name) {
        Server server = new Server(c, id);
        connections.put(name, server);
        activeServer = name;
    }

    public void setHandler(String server, Handler handler) {
        connections.get(server).setHandler(handler);
    }

    public static void networkAvailable() {
        //TODO: notify the user we are reconnecting and joining channels.
        for (Server s : connections.values()) {
            s.connectAndLoadState();
        }
    }

    public static void networkUnavailable() {
        //TODO: pop up a dialog notifying the user we have disconnected and saved state.
        IS_CONNECTED = false;
        for (Server s : connections.values()) {
            s.closeConnection();
        }
    }
}
