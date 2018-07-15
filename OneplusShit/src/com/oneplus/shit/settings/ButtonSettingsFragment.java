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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.SwitchPreference;
import android.util.Log;

import java.util.Arrays;

public class ButtonSettingsFragment extends NodePreferenceFragment {
    private static final String TAG = "ButtonSettingsFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.button_panel);
        if (ButtonUtils.isSliderSupported()) {
            initNotificationSliderPreference();
        } else {
            getPreferenceScreen().removePreference(findPreference(
                    ButtonConstants.SLIDER_PANEL_KEY));
        }
        if (!ButtonUtils.isHardwareKeysSupported()) {
            getPreferenceScreen().removePreference(findPreference(
                    ButtonConstants.BUTTON_SWAP_KEY));
        }
    }

    private void initNotificationSliderPreference() {
        registerPreferenceListener(ButtonConstants.SLIDER_USAGE_KEY);
        registerPreferenceListener(ButtonConstants.SLIDER_ACTION_TOP_KEY);
        registerPreferenceListener(ButtonConstants.SLIDER_ACTION_MIDDLE_KEY);
        registerPreferenceListener(ButtonConstants.SLIDER_ACTION_BOTTOM_KEY);

        ListPreference usagePref = (ListPreference) findPreference(
                ButtonConstants.SLIDER_USAGE_KEY);
        handleSliderUsageChange(usagePref.getValue());
    }

    private void registerPreferenceListener(String key) {
        Preference p = findPreference(key);
        p.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case ButtonConstants.SLIDER_USAGE_KEY:
                return handleSliderUsageChange((String) newValue) &&
                        handleSliderUsageDefaultsChange((String) newValue) &&
                        notifySliderUsageChange((String) newValue);
            case ButtonConstants.SLIDER_ACTION_TOP_KEY:
                return notifySliderActionChange(0, (String) newValue);
            case ButtonConstants.SLIDER_ACTION_MIDDLE_KEY:
                return notifySliderActionChange(1, (String) newValue);
            case ButtonConstants.SLIDER_ACTION_BOTTOM_KEY:
                return notifySliderActionChange(2, (String) newValue);
            default:
                return super.onPreferenceChange(preference, newValue);
        }
    }

    private boolean handleSliderUsageChange(String newValue) {
        switch (newValue) {
            case ButtonConstants.SLIDER_FOR_NOTIFICATION:
                return updateSliderActions(
                        R.array.notification_slider_mode_entries,
                        R.array.notification_slider_mode_entry_values);
            case ButtonConstants.SLIDER_FOR_FLASHLIGHT:
                return updateSliderActions(
                        R.array.notification_slider_flashlight_entries,
                        R.array.notification_slider_flashlight_entry_values);
            case ButtonConstants.SLIDER_FOR_BRIGHTNESS:
                return updateSliderActions(
                        R.array.notification_slider_brightness_entries,
                        R.array.notification_slider_brightness_entry_values);
            case ButtonConstants.SLIDER_FOR_ROTATION:
                return updateSliderActions(
                        R.array.notification_slider_rotation_entries,
                        R.array.notification_slider_rotation_entry_values);
            case ButtonConstants.SLIDER_FOR_RINGER:
                return updateSliderActions(
                        R.array.notification_slider_ringer_entries,
                        R.array.notification_slider_ringer_entry_values);
            case ButtonConstants.SLIDER_FOR_CAFFEINE:
                return updateSliderActions(
                        R.array.notification_slider_caffeine_entries,
                        R.array.notification_slider_caffeine_entry_values);
            default:
                return false;
        }
    }

    private boolean handleSliderUsageDefaultsChange(String newValue) {
        int defaultsResId = getDefaultResIdForUsage(newValue);
        if (defaultsResId == 0) {
            return false;
        }
        return updateSliderActionDefaults(defaultsResId);
    }

    private boolean updateSliderActions(int entriesResId, int entryValuesResId) {
        String[] entries = getResources().getStringArray(entriesResId);
        String[] entryValues = getResources().getStringArray(entryValuesResId);
        return updateSliderPreference(
                ButtonConstants.SLIDER_ACTION_TOP_KEY,
                entries, entryValues) &&
            updateSliderPreference(
                    ButtonConstants.SLIDER_ACTION_MIDDLE_KEY,
                    entries, entryValues) &&
            updateSliderPreference(
                    ButtonConstants.SLIDER_ACTION_BOTTOM_KEY,
                    entries, entryValues);
    }

    private boolean updateSliderActionDefaults(int defaultsResId) {
        String[] defaults = getResources().getStringArray(defaultsResId);
        if (defaults.length != 3) {
            return false;
        }

        return updateSliderPreferenceValue(
                ButtonConstants.SLIDER_ACTION_TOP_KEY,
                defaults[0]) &&
            updateSliderPreferenceValue(
                    ButtonConstants.SLIDER_ACTION_MIDDLE_KEY,
                    defaults[1]) &&
            updateSliderPreferenceValue(
                    ButtonConstants.SLIDER_ACTION_BOTTOM_KEY,
                    defaults[2]);
    }

    private boolean updateSliderPreference(CharSequence key,
            String[] entries, String[] entryValues) {
        ListPreference pref = (ListPreference) findPreference(key);
        if (pref == null) {
            return false;
        }
        pref.setEntries(entries);
        pref.setEntryValues(entryValues);
        return true;
    }

    private boolean updateSliderPreferenceValue(CharSequence key,
            String value) {
        ListPreference pref = (ListPreference) findPreference(key);
        if (pref == null) {
            return false;
        }
        pref.setValue(value);
        return true;
    }

    private int[] getCurrentSliderActions() {
        int[] actions = new int[3];
        ListPreference p;

        p = (ListPreference) findPreference(
                ButtonConstants.SLIDER_ACTION_TOP_KEY);
        actions[0] = Integer.parseInt(p.getValue());

        p = (ListPreference) findPreference(
                ButtonConstants.SLIDER_ACTION_MIDDLE_KEY);
        actions[1] = Integer.parseInt(p.getValue());

        p = (ListPreference) findPreference(
                ButtonConstants.SLIDER_ACTION_BOTTOM_KEY);
        actions[2] = Integer.parseInt(p.getValue());

        return actions;
    }

    private boolean notifySliderUsageChange(String usage) {
        sendUpdateBroadcast(getContext(), Integer.parseInt(usage),
                getCurrentSliderActions());
        return true;
    }

    private boolean notifySliderActionChange(int index, String value) {
        ListPreference p = (ListPreference) findPreference(
                ButtonConstants.SLIDER_USAGE_KEY);
        int usage = Integer.parseInt(p.getValue());

        int[] actions = getCurrentSliderActions();
        actions[index] = Integer.parseInt(value);

        sendUpdateBroadcast(getContext(), usage, actions);
        return true;
    }

    public static void sendUpdateBroadcast(Context context,
            int usage, int[] actions) {
        Intent intent = new Intent(ButtonConstants.ACTION_UPDATE_SLIDER_SETTINGS);
        intent.putExtra(ButtonConstants.EXTRA_SLIDER_USAGE, usage);
        intent.putExtra(ButtonConstants.EXTRA_SLIDER_ACTIONS, actions);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        Log.d(TAG, "update slider usage " + usage + " with actions: " +
                Arrays.toString(actions));
    }

    public static void restoreSliderStates(Context context) {
        Resources res = context.getResources();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String usage = prefs.getString(ButtonConstants.SLIDER_USAGE_KEY,
                res.getString(R.string.config_defaultNotificationSliderUsage));

        int defaultsResId = getDefaultResIdForUsage(usage);
        if (defaultsResId == 0) {
            return;
        }

        String[] defaults = res.getStringArray(defaultsResId);
        if (defaults.length != 3) {
            return;
        }

        String actionTop = prefs.getString(
                ButtonConstants.SLIDER_ACTION_TOP_KEY, defaults[0]);

        String actionMiddle = prefs.getString(
                ButtonConstants.SLIDER_ACTION_MIDDLE_KEY, defaults[1]);

        String actionBottom = prefs.getString(
                ButtonConstants.SLIDER_ACTION_BOTTOM_KEY, defaults[2]);

        prefs.edit()
            .putString(ButtonConstants.SLIDER_USAGE_KEY, usage)
            .putString(ButtonConstants.SLIDER_ACTION_TOP_KEY, actionTop)
            .putString(ButtonConstants.SLIDER_ACTION_MIDDLE_KEY, actionMiddle)
            .putString(ButtonConstants.SLIDER_ACTION_BOTTOM_KEY, actionBottom)
            .commit();

        sendUpdateBroadcast(context, Integer.parseInt(usage), new int[] {
            Integer.parseInt(actionTop),
            Integer.parseInt(actionMiddle),
            Integer.parseInt(actionBottom)
        });
    }

    private static int getDefaultResIdForUsage(String usage) {
        switch (usage) {
            case ButtonConstants.SLIDER_FOR_NOTIFICATION:
                return R.array.config_defaultSliderActionsForNotification;
            case ButtonConstants.SLIDER_FOR_FLASHLIGHT:
                return R.array.config_defaultSliderActionsForFlashlight;
            case ButtonConstants.SLIDER_FOR_BRIGHTNESS:
                return R.array.config_defaultSliderActionsForBrightness;
            case ButtonConstants.SLIDER_FOR_ROTATION:
                return R.array.config_defaultSliderActionsForRotation;
            case ButtonConstants.SLIDER_FOR_RINGER:
                return R.array.config_defaultSliderActionsForRinger;
            case ButtonConstants.SLIDER_FOR_CAFFEINE:
                return R.array.config_defaultSliderActionsForCaffeine;
            default:
                return 0;
        }
    }

}
