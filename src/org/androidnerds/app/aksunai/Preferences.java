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
