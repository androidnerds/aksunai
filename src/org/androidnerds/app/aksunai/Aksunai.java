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
package org.androidnerds.app.aksunai;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidnerds.app.aksunai.data.ServerDbAdapter;
import org.androidnerds.app.aksunai.preferences.Preferences;
import org.androidnerds.app.aksunai.ui.ChatActivity;
import org.androidnerds.app.aksunai.ui.ServerDetail;

import java.util.Vector;

public class Aksunai extends ListActivity {
    ServerListAdapter mAdapter;
    private static final int NEW_SERVER = Menu.FIRST;
    private static final int SETTINGS = Menu.FIRST + 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        setTitle(R.string.server_activity_title);

        initAdapter();
        registerForContextMenu(getListView());
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, NEW_SERVER, 1, R.string.menu_new_server).setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, SETTINGS, 2, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case NEW_SERVER:
            newServer();
            break;
        case SETTINGS:
            startActivity(new Intent(this, Preferences.class));
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        super.onListItemClick(l, v, pos, id);

        if (pos == 0) {
            newServer();
        } else {
        	Intent i = new Intent(Aksunai.this, ChatActivity.class);
        	i.putExtra("id", id);
            i.putExtra("title", mAdapter.getTitle(pos - 1));
            startActivity(i);
        }
    }

    /* keep in mind the onCreateContextMenu is called before each draw to the screen. */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo i) {
        //we use this to grab the list view position. if zero no menu is needed.
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) i;

        if (info.position == 0) {
            return;
        }

        menu.setHeaderTitle(R.string.menu_server_options);

        menu.add(R.string.connect);
        menu.add(R.string.edit);
        menu.add(R.string.remove);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getTitle().equals(getString(R.string.remove))) {
            ServerDbAdapter db = new ServerDbAdapter(this);
            db.deleteServer(mAdapter.mIds.get(info.position - 1));
            mAdapter.resetData();
        }

        if (item.getTitle().equals(getString(R.string.edit))) {
            Intent i = new Intent(this, ServerDetail.class);
            i.putExtra("mode", "edit");
            i.putExtra("id", mAdapter.mIds.get(info.position - 1));
            startActivity(i);
        }

        if (item.getTitle().equals(getString(R.string.connect))) {
            connectToServer(mAdapter.mIds.get(info.position - 1), mAdapter.getTitle(info.position - 1));
        }

        if (item.getTitle().equals(getString(R.string.menu_disconnect))) {
            //ConnectionService.disconnectFromServer(mAdapter.getTitle(info.position - 1));
        }

        return false;
    }

    public void newServer() {
        Intent i = new Intent(this, ServerDetail.class);
        i.putExtra("mode", "create");
        startActivity(i);
    }

    public void connectToServer(long id, String name) {
        //determine network state before connecting.
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            Toast.makeText(Aksunai.this, getString(R.string.no_network_connection), Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(Aksunai.this, ChatActivity.class);
        i.putExtra("id", id);
        i.putExtra("title", name);
        startActivity(i);
    }

    public void initAdapter() {
        //setup the header item so the new option is always on top.
        LayoutInflater inflate = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.server_row, getListView(), false);

        TextView title = (TextView) view.findViewById(R.id.server_title);
        TextView address = (TextView) view.findViewById(R.id.server_address);

        title.setText(R.string.new_server);
        address.setText(R.string.create_new_server);
        getListView().addHeaderView(view);

        mAdapter = new ServerListAdapter(this);
        mAdapter.loadData();

        setListAdapter(mAdapter);
    }


    private static class ServerListAdapter extends BaseAdapter {
        private Context mCtx;
        private Vector<String> mTitles;
        private Vector<String> mAddresses;
        private Vector<Long> mIds;

        public ServerListAdapter(Context c) {
            mCtx = c;
        }

        public int getCount() {
            return mTitles.size();
        }

        public Object getItem(int pos) {
            return mAddresses.elementAt(pos);
        }

        public String getTitle(int pos) {
            return mTitles.elementAt(pos);
        }

        public long getItemId(int pos) {
            return mIds.elementAt(pos).longValue();
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflate = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflate.inflate(R.layout.server_row, parent, false);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.server_title);
                holder.url = (TextView) convertView.findViewById(R.id.server_address);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(mTitles.elementAt(pos));
            holder.url.setText(mAddresses.elementAt(pos));


            return convertView;
        }

        public void loadData() {
            ServerDbAdapter db = new ServerDbAdapter(mCtx);

            mIds = db.getIds();
            mTitles = db.getTitles();
            mAddresses = db.getAddresses();

            db.close();

            notifyDataSetInvalidated();
        }

        public void resetData() {
            mIds.clear();
            mTitles.clear();
            mAddresses.clear();

            notifyDataSetInvalidated();
            loadData();
        }


        static class ViewHolder {
            TextView title;
            TextView url;
        }

    };
}
