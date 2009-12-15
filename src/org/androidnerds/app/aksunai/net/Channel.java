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

import java.util.ArrayList;

public class Channel {

    public enum TYPE { PM, SERVER, CHANNEL }

    public TYPE type;
    public ArrayList<String> conversation;
    public ArrayList<String> users;
    public String name;
    public String topic;
    
    private static final int SCROLLBACK_SIZE = 30;

    public Channel(String name, TYPE type) {
        conversation = new ArrayList<String>();
        users = new ArrayList<String>();
        this.name = name;
        this.type = type;
    }

    public void addLine(String line) {
        if (line != null) {
            if (line != "") {
                conversation.add(line);
            }
        }

        if (conversation.size() > SCROLLBACK_SIZE) {
            conversation.remove(0);
        }
    }

    public String toString() {
        return name;
    }
}
