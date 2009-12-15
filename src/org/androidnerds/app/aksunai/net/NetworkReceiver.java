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
package org.androidnerds.app.aksunai.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;

/* This code determines what's going on with the network state to save the application state. */
public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent i) {
        NetworkInfo n = (NetworkInfo) i.getExtras().get("networkInfo");

        //TODO: find out what happens when a phone call comes in because that is crashing the app.
        if (n.isConnected()) {
            if (ConnectionService.IS_RUNNING && !ConnectionService.IS_CONNECTED) {
                ConnectionService.networkAvailable();
            }
        } else {
            if (ConnectionService.IS_RUNNING && ConnectionService.IS_CONNECTED) {
                ConnectionService.networkUnavailable();
            }
        }
    }
}
