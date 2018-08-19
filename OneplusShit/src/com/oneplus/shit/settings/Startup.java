/*
 * Copyright (C) 2017 The MoKee Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.oneplus.shit.settings.KernelControl;
import com.oneplus.shit.settings.ScreenOffGesture;

import java.io.File;

public class Startup extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
            ButtonSettingsActivity.restoreState(context);

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                enableComponent(context, ScreenOffGesture.class.getName());
                SharedPreferences screenOffGestureSharedPreferences = context.getSharedPreferences(
                        ScreenOffGesture.GESTURE_SETTINGS, Activity.MODE_PRIVATE);
                KernelControl.enableGestures(
                        screenOffGestureSharedPreferences.getBoolean(
                        ScreenOffGesture.PREF_GESTURE_ENABLE, true));
         }
    }

    private boolean getPreferenceBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, defaultValue);
    }

    private void enableComponent(Context context, String component) {
        ComponentName name = new ComponentName(context, component);
        PackageManager pm = context.getPackageManager();
        if (pm.getComponentEnabledSetting(name)
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(name,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}
