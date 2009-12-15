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

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.androidnerds.app.aksunai.MyConfig;
import org.androidnerds.app.aksunai.R;
import org.androidnerds.app.aksunai.net.ConnectionService;
import org.androidnerds.app.aksunai.net.Server;

@SuppressWarnings(value = { "unchecked" })
public class UserList extends ListActivity {

    private Server mServer;
    private UserListAdapter mAdapter;
    private EditText mSearchBox;

    @Override
    public void onCreate(Bundle appState) {
        super.onCreate(appState);

        mServer = ConnectionService.connections.get(ConnectionService.activeServer);
        mServer.setUserHandler(mHandler);

        mAdapter = new UserListAdapter(this, R.id.user_list_name, mServer);

        setTitle(mServer.activeChannel.name);
        setContentView(R.layout.user_list);

        mSearchBox = (EditText) findViewById(R.id.nicksearch);
        mSearchBox.addTextChangedListener(mWatcher);

        setListAdapter(mAdapter);
        getListView().setTextFilterEnabled(true);

        registerForContextMenu(getListView());
    }

    @Override
    public void onPause() {
        super.onPause();

        mServer.freeUserHandler();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
        menu.setHeaderTitle(mServer.activeChannel.name + " " + getString(R.string.options));

        menu.add(getString(R.string.private_message));
        menu.add(getString(R.string.info));
        menu.add(getString(R.string.ignore));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getTitle().equals(getString(R.string.private_message))) {
            mServer.openNewPM(mAdapter.getItem(info.position));
            finish();
        }

        return false;
    }

    private TextWatcher mWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {

        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (MyConfig.DEBUG) Log.d("Aksunai", "Watching text...");
            mAdapter.getFilter().filter(s);
            mAdapter.notifyDataSetChanged();
        }
    };


    private static class UserListAdapter extends ArrayAdapter<String> implements Filterable {

        private Context mCtx;
        private final Object mLock = new Object();
        private List<String> mObjects;
        private List<String> mOriginalObjects;
        private ArrayFilter mFilter;
        private LayoutInflater mInflater;
        private Server mServer;

        public UserListAdapter(Context c, int res, Server server) {
            super(c, res, server.activeChannel.users);
            mServer = server;
            mCtx = c;
            mObjects = server.activeChannel.users;
            mOriginalObjects = server.activeChannel.users;
            mInflater = LayoutInflater.from(mCtx);
        }

        public int getCount() {
            return mObjects.size();
        }

        public long getItemId(int pos) {
            return pos;
        }

        public String getItem(int pos) {
            return mObjects.get(pos);
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.user_list_row, parent, false);

                holder = new ViewHolder();

                holder.text = (TextView)convertView.findViewById(R.id.user_list_name);
                holder.icon = (ImageView)convertView.findViewById(R.id.user_list_icon);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            String user = mObjects.get(pos);

            if (user.startsWith("@")) {
                holder.icon.setImageResource(R.drawable.user_op_icon);
                holder.text.setText(user.substring(1));
            } else {
                holder.icon.setImageResource(R.drawable.user_icon);
                holder.text.setText(user);
            }

            return convertView;
        }

        public void updateData(ArrayList<String> arr) {
            mOriginalObjects = arr;
            notifyDataSetChanged();
        }

        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new ArrayFilter();
            }

            return mFilter;
        }

        static class ViewHolder {
            TextView text;
            ImageView icon;
        }


        private class ArrayFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                if (MyConfig.DEBUG) Log.d("Aksunai", "Are we performing filtering yet?");
                FilterResults results = new FilterResults();

                if (mObjects == null) {
                    synchronized (mLock) {
                        mObjects = new ArrayList<String>(mServer.activeChannel.users);
                    }
                }

                if (prefix == null || prefix.length() == 0) {
                    synchronized (mLock) {
                        ArrayList<String> list = new ArrayList<String>(mOriginalObjects);
                        results.values = list;
                        results.count = list.size();
                    }
                } else {
                    String prefixString = prefix.toString().toLowerCase();

                    final ArrayList<String> values = new ArrayList<String>(mObjects);
                    final int count = values.size();

                    final ArrayList<String> newValues = new ArrayList<String>(count);

                    for (int i = 0; i < count; i++) {
                        final String value = values.get(i);
                        final String valueText = value.toString().toLowerCase();

                        if (valueText.startsWith(prefixString)) {
                            newValues.add(value);
                        } else {
                            final String[] words = valueText.split(" ");
                            final int wordCount = words.length;

                            for (int k = 0; k < wordCount; k++) {
                                if (words[k].startsWith(prefixString)) {
                                    newValues.add(value);
                                    break;
                                }
                            }
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();

                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mObjects = (List<String>)results.values;

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    mObjects = mServer.activeChannel.users;
                    notifyDataSetInvalidated();
                }
            }
        }
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case Server.MSG_DATA_CHANGED:
                if (mServer.activeChannel.name.equals(msg.obj) || msg.obj.equals("quit")) {
                    mAdapter.updateData(mServer.activeChannel.users);
                }
                break;
            }
        }
    };
}
