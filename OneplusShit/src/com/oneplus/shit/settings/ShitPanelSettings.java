/*
 * Copyright (C) 2015 The CyanogenMod Project
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

package com.oneplus.shit.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;

import com.oneplus.shit.settings.R;

public class ShitPanelSettings extends PreferenceActivity implements
                                  Preference.OnPreferenceChangeListener {

    public static final String KEY_VIBSTRENGTH = "vib_strength";

    /*public static final String KEY_TAPTOWAKE_SWITCH = "taptowake";*/

    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_DCI_SWITCH = "dci";
    public static final String KEY_NIGHT_SWITCH = "night";
    public static final String KEY_ONEPLUS_SWITCH = "oneplus";
    private static final String SPECTRUM_KEY = "spectrum";
    private static final String SPECTRUM_SYSTEM_PROPERTY = "persist.spectrum.profile";

    private VibratorStrengthPreference mVibratorStrength;
    private TwoStatePreference mHBMModeSwitch;
    private ListPreference mSpectrum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.shit_panel);

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null) {
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        }

        /*mTapToWakeSwitch = (TwoStatePreference) findPreference(KEY_TAPTOWAKE_SWITCH);
        mTapToWakeSwitch.setOnPreferenceChangeListener(new TapToWakeSwitch());*/

        mHBMModeSwitch = (TwoStatePreference) findPreference(KEY_HBM_SWITCH);
        mHBMModeSwitch.setOnPreferenceChangeListener(new HBMModeSwitch());

        mSpectrum = (ListPreference) findPreference(SPECTRUM_KEY);
        if( mSpectrum != null ) {
            mSpectrum.setValue(SystemProperties.get(SPECTRUM_SYSTEM_PROPERTY, "0"));
            mSpectrum.setOnPreferenceChangeListener(this);
        }
    }

     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         final String key = preference.getKey();
         boolean value;
         String strvalue;
         if (SPECTRUM_KEY.equals(key)) {
            strvalue = (String) newValue;
            SystemProperties.set(SPECTRUM_SYSTEM_PROPERTY, strvalue);
            return true;
         }
        return true;
     }
}
