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

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;

public class Preferences extends PreferenceActivity {

    public Preferences() {

    }

    protected int resourceId() {
        return R.xml.preferences;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName("aksunai-prefs");
        manager.setSharedPreferencesMode(MODE_WORLD_WRITEABLE);
        this.addPreferencesFromResource(resourceId());

        Preference p = findPreference("pref_large_font_label");
        if (p != null) {
            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference p, Object newObjValue) {
                    Boolean newValue = (Boolean) newObjValue;
                    if (newValue == null) {
                        return false;
                    }

                    return true;
                }
            });
        }
    }
}
