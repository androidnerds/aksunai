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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Hashtable;
import java.util.Vector;

public class ServerDbAdapter extends SQLiteOpenHelper {

    private static final String DB_NAME = "servers.db";
    private static final int DB_VERSION = 4;
    private static final String DB_TABLE = "server_list";

    private static final String DB_CREATE = "create table server_list (_id INTEGER PRIMARY KEY, title TEXT NOT NULL, address TEXT NOT NULL, username TEXT NOT NULL, nick TEXT NOT NULL, password TEXT, port TEXT, real_name TEXT, autojoin TEXT, autoconnect INTEGER);";

    private SQLiteDatabase mDb;
    private Cursor mCur;

    public ServerDbAdapter(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        assert(newVersion == DB_VERSION);

        switch (oldVersion) {
        case 2:
            db.execSQL("ALTER TABLE server_list ADD COLUMN port TEXT;");
            db.execSQL("ALTER TABLE server_list ADD COLUMN real_name TEXT;");
            break;
        case 3:
        	db.execSQL("ALTER TABLE server_list ADD COLUMN autojoin TEXT;");
        	db.execSQL("ALTER TABLE server_list ADD COLUMN autoconnect INTEGER;");
        	break;
        default:
            db.execSQL("DROP TABLE IF EXISTS server_list");
            onCreate(db);
            break;
        }
    }

    public long addServer(String title, String address, String username, String nick, String password, String port, String realName, String autoJoin, int autoConnect) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("address", address);
        values.put("username", username);
        values.put("nick", nick);
        values.put("password", password);
        values.put("port", port);
        values.put("real_name", realName);
        values.put("autojoin", autoJoin);
        values.put("autoconnect", autoConnect);
        long id = db.insert(DB_TABLE, null, values);

        db.close();

        return id;
    }

    public int deleteServer(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(DB_TABLE, "_id = ?", new String[] { String.valueOf(id) });
        db.close();

        return result;
    }

    public int updateServer(int id, String title, String address, String username, String nick, String password, String port, String realName, String autoJoin, int autoConnect) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("address", address);
        values.put("username", username);
        values.put("nick", nick);
        values.put("password", password);
        values.put("port", port);
        values.put("real_name", realName);
        values.put("autojoin", autoJoin);
        values.put("autoconnect", autoConnect);
        
        int result = db.update(DB_TABLE, values, "_id = ?", new String[] { String.valueOf(id) });

        db.close();
        return result;
    }

    public Vector<Long> getIds() {
        SQLiteDatabase db = getReadableDatabase();
        Vector<Long> ids = new Vector<Long>();

        Cursor c = db.query(DB_TABLE, null, null, null, null, null, null);

        while (c.moveToNext()) {
            ids.add(new Long(c.getLong(0)));
        }

        c.close();
        db.close();

        return ids;
    }

    public Vector<String> getTitles() {
        SQLiteDatabase db = getReadableDatabase();
        Vector<String> titles = new Vector<String>();

        Cursor c = db.query(DB_TABLE, null, null, null, null, null, null);

        while (c.moveToNext()) {
            titles.add(c.getString(1));
        }

        c.close();
        db.close();

        return titles;
    }

    public Vector<String> getAddresses() {
        SQLiteDatabase db = getReadableDatabase();
        Vector<String> addresses = new Vector<String>();

        Cursor c = db.query(DB_TABLE, null, null, null, null, null, null);

        while (c.moveToNext()) {
            addresses.add(c.getString(2));
        }

        c.close();
        db.close();

        return addresses;
    }

    public Hashtable<Long, String> getAutoConnectServers() {
    	SQLiteDatabase db = getReadableDatabase();
    	Hashtable<Long, String> servers = new Hashtable<Long, String>();
    	
    	Cursor c = db.query(DB_TABLE, null, null, null, null, null, null);
    	
    	while (c.moveToNext()) {
    		servers.put(new Long(c.getLong(8)), c.getString(1));
    	}
    	
    	c.close();
    	db.close();
    	
    	return servers;
    }
    
    public Cursor getItem(long id) {
        mDb = getReadableDatabase();

        mCur = mDb.query(DB_TABLE, null, "_id = ?", new String[] { String.valueOf(id) }, null, null, null);

        return mCur;
    }

    public void release() {
        mCur.close();
        mDb.close();
    }
}
