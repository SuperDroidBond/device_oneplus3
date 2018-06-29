/*
 * Copyright (C) 2014 Slimroms
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
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.util.gzosp.AppHelper;
import com.android.internal.util.gzosp.ActionConstants;
import com.android.internal.util.gzosp.DeviceUtils;
import com.android.internal.util.gzosp.DeviceUtils.FilteredDeviceFeaturesArray;

import com.oneplus.shit.KernelControl;
import com.oneplus.shit.R;
import com.oneplus.shit.util.ShortcutPickerHelper;

public class ScreenOffGesture extends PreferenceFragment implements
        OnPreferenceChangeListener, OnPreferenceClickListener,
        ShortcutPickerHelper.OnPickListener {

    private static final String SETTINGS_METADATA_NAME = "com.android.settings";

    public static final String GESTURE_SETTINGS = "screen_off_gesture_settings";

    public static final String PREF_GESTURE_ENABLE = "enable_gestures";
    public static final String PREF_GESTURE_CIRCLE = "gesture_circle";
    public static final String PREF_GESTURE_DOUBLE_SWIPE = "gesture_double_swipe";
    public static final String PREF_GESTURE_ARROW_UP = "gesture_arrow_up";
    public static final String PREF_GESTURE_ARROW_DOWN = "gesture_arrow_down";
    public static final String PREF_GESTURE_ARROW_LEFT = "gesture_arrow_left";
    public static final String PREF_GESTURE_ARROW_RIGHT = "gesture_arrow_right";

    private static final int DLG_SHOW_ACTION_DIALOG  = 0;
    private static final int DLG_RESET_TO_DEFAULT    = 1;

    private static final int MENU_RESET = Menu.FIRST;

    private Preference mGestureCircle;
    private Preference mGestureDoubleSwipe;
    private Preference mGestureArrowUp;
    private Preference mGestureArrowDown;
    private Preference mGestureArrowLeft;
    private Preference mGestureArrowRight;
    private SwitchPreference mEnableGestures;

    private boolean mCheckPreferences;
    private SharedPreferences mScreenOffGestureSharedPreferences;

    private ShortcutPickerHelper mPicker;
    private String mPendingSettingsKey;
    private static FilteredDeviceFeaturesArray sFinalActionDialogArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPicker = new ShortcutPickerHelper(getActivity(), this);

        mScreenOffGestureSharedPreferences = getActivity().getSharedPreferences(
                GESTURE_SETTINGS, Activity.MODE_PRIVATE);

        // Before we start filter out unsupported options on the
        // ListPreference values and entries
        PackageManager pm = getActivity().getPackageManager();
        Resources settingsResources = null;
        try {
            settingsResources = pm.getResourcesForApplication(SETTINGS_METADATA_NAME);
        } catch (Exception e) {
            return;
        }
        sFinalActionDialogArray = new FilteredDeviceFeaturesArray();
        sFinalActionDialogArray = DeviceUtils.filterUnsupportedDeviceFeatures(getActivity(),
                settingsResources.getStringArray(
                        settingsResources.getIdentifier(SETTINGS_METADATA_NAME
                        + ":array/shortcut_action_screen_off_values", null, null)),
                settingsResources.getStringArray(
                        settingsResources.getIdentifier(SETTINGS_METADATA_NAME
                        + ":array/shortcut_action_screen_off_entries", null, null)));

        // Attach final settings screen.
        reloadSettings();

        setHasOptionsMenu(true);
    }

    private PreferenceScreen reloadSettings() {
        mCheckPreferences = false;
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.screen_off_gesture);
        prefs = getPreferenceScreen();

        mEnableGestures = (SwitchPreference) prefs.findPreference(PREF_GESTURE_ENABLE);

        mGestureCircle = (Preference) prefs.findPreference(PREF_GESTURE_CIRCLE);
        mGestureDoubleSwipe = (Preference) prefs.findPreference(PREF_GESTURE_DOUBLE_SWIPE);
        mGestureArrowUp = (Preference) prefs.findPreference(PREF_GESTURE_ARROW_UP);
        mGestureArrowDown = (Preference) prefs.findPreference(PREF_GESTURE_ARROW_DOWN);
        mGestureArrowLeft = (Preference) prefs.findPreference(PREF_GESTURE_ARROW_LEFT);
        mGestureArrowRight = (Preference) prefs.findPreference(PREF_GESTURE_ARROW_RIGHT);

        setupOrUpdatePreference(mGestureCircle, mScreenOffGestureSharedPreferences
                .getString(PREF_GESTURE_CIRCLE, ActionConstants.ACTION_CAMERA));
        setupOrUpdatePreference(mGestureDoubleSwipe, mScreenOffGestureSharedPreferences
                .getString(PREF_GESTURE_DOUBLE_SWIPE, ActionConstants.ACTION_MEDIA_PLAY_PAUSE));
        setupOrUpdatePreference(mGestureArrowUp, mScreenOffGestureSharedPreferences
                    .getString(PREF_GESTURE_ARROW_UP, ActionConstants.ACTION_TORCH));
        setupOrUpdatePreference(mGestureArrowDown, mScreenOffGestureSharedPreferences
                .getString(PREF_GESTURE_ARROW_DOWN, ActionConstants.ACTION_VIB_SILENT));
        setupOrUpdatePreference(mGestureArrowLeft, mScreenOffGestureSharedPreferences
                .getString(PREF_GESTURE_ARROW_LEFT, ActionConstants.ACTION_MEDIA_PREVIOUS));
        setupOrUpdatePreference(mGestureArrowRight, mScreenOffGestureSharedPreferences
                .getString(PREF_GESTURE_ARROW_RIGHT, ActionConstants.ACTION_MEDIA_NEXT));

        boolean enableGestures =
                mScreenOffGestureSharedPreferences.getBoolean(PREF_GESTURE_ENABLE, true);
        mEnableGestures.setChecked(enableGestures);
        mEnableGestures.setOnPreferenceChangeListener(this);

        mCheckPreferences = true;
        return prefs;
    }

    private void setupOrUpdatePreference(Preference preference, String action) {
        if (preference == null || action == null) {
            return;
        }

        if (action.startsWith("**")) {
            preference.setSummary(getDescription(action));
        } else {
            preference.setSummary(AppHelper.getFriendlyNameForUri(
                    getActivity(), getActivity().getPackageManager(), action));
        }
        preference.setOnPreferenceClickListener(this);
    }

    private String getDescription(String action) {
        if (sFinalActionDialogArray == null || action == null) {
            return null;
        }
        int i = 0;
        for (String actionValue : sFinalActionDialogArray.values) {
            if (action.equals(actionValue)) {
                return sFinalActionDialogArray.entries[i];
            }
            i++;
        }
        return null;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String settingsKey = null;
        int dialogTitle = 0;
        if (preference == mGestureCircle) {
            settingsKey = PREF_GESTURE_CIRCLE;
            dialogTitle = R.string.gesture_circle_title;
        } else if (preference == mGestureDoubleSwipe) {
            settingsKey = PREF_GESTURE_DOUBLE_SWIPE;
            dialogTitle = R.string.gesture_double_swipe_title;
        } else if (preference == mGestureArrowUp) {
            settingsKey = PREF_GESTURE_ARROW_UP;
            dialogTitle = R.string.gesture_arrow_up_title;
        } else if (preference == mGestureArrowDown) {
            settingsKey = PREF_GESTURE_ARROW_DOWN;
            dialogTitle = R.string.gesture_arrow_down_title;
        } else if (preference == mGestureArrowLeft) {
            settingsKey = PREF_GESTURE_ARROW_LEFT;
            dialogTitle = R.string.gesture_arrow_left_title;
        } else if (preference == mGestureArrowRight) {
            settingsKey = PREF_GESTURE_ARROW_RIGHT;
            dialogTitle = R.string.gesture_arrow_right_title;
        }
        if (settingsKey != null) {
            showDialogInner(DLG_SHOW_ACTION_DIALOG, settingsKey, dialogTitle);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }
        if (preference == mEnableGestures) {
            mScreenOffGestureSharedPreferences.edit()
                    .putBoolean(PREF_GESTURE_ENABLE, (Boolean) newValue).commit();
            KernelControl.enableGestures((Boolean) newValue);
            return true;
        }
        return false;
    }

    // Reset all entries to default.
    private void resetToDefault() {
        SharedPreferences.Editor editor = mScreenOffGestureSharedPreferences.edit();
        mScreenOffGestureSharedPreferences.edit()
                .putBoolean(PREF_GESTURE_ENABLE, true).commit();
        editor.putString(PREF_GESTURE_CIRCLE,
                ActionConstants.ACTION_CAMERA).commit();
        editor.putString(PREF_GESTURE_DOUBLE_SWIPE,
                ActionConstants.ACTION_MEDIA_PLAY_PAUSE).commit();
        editor.putString(PREF_GESTURE_ARROW_UP,
                ActionConstants.ACTION_TORCH).commit();
        editor.putString(PREF_GESTURE_ARROW_DOWN,
                ActionConstants.ACTION_VIB_SILENT).commit();
        editor.putString(PREF_GESTURE_ARROW_LEFT,
                ActionConstants.ACTION_MEDIA_PREVIOUS).commit();
        editor.putString(PREF_GESTURE_ARROW_RIGHT,
                ActionConstants.ACTION_MEDIA_NEXT).commit();
        editor.commit();
        KernelControl.enableGestures(true);
        reloadSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void shortcutPicked(String action,
                String description, Bitmap bmp, boolean isApplication) {
        if (mPendingSettingsKey == null || action == null) {
            return;
        }
        mScreenOffGestureSharedPreferences.edit().putString(mPendingSettingsKey, action).commit();
        reloadSettings();
        mPendingSettingsKey = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            }
        } else {
            mPendingSettingsKey = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                    showDialogInner(DLG_RESET_TO_DEFAULT, null, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private void showDialogInner(int id, String settingsKey, int dialogTitle) {
        DialogFragment newFragment =
                MyAlertDialogFragment.newInstance(id, settingsKey, dialogTitle);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(
                int id, String settingsKey, int dialogTitle) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putString("settingsKey", settingsKey);
            args.putInt("dialogTitle", dialogTitle);
            frag.setArguments(args);
            return frag;
        }

        ScreenOffGesture getOwner() {
            return (ScreenOffGesture) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            final String settingsKey = getArguments().getString("settingsKey");
            int dialogTitle = getArguments().getInt("dialogTitle");
            switch (id) {
                case DLG_SHOW_ACTION_DIALOG:
                    if (sFinalActionDialogArray == null) {
                        return null;
                    }
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(dialogTitle)
                    .setNegativeButton(R.string.cancel, null)
                    .setItems(getOwner().sFinalActionDialogArray.entries,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (getOwner().sFinalActionDialogArray.values[item]
                                    .equals(ActionConstants.ACTION_APP)) {
                                if (getOwner().mPicker != null) {
                                    getOwner().mPendingSettingsKey = settingsKey;
                                    getOwner().mPicker.pickShortcut(getOwner().getId());
                                }
                            } else {
                                getOwner().mScreenOffGestureSharedPreferences.edit()
                                        .putString(settingsKey,
                                        getOwner().sFinalActionDialogArray.values[item]).commit();
                                getOwner().reloadSettings();
                            }
                        }
                    })
                    .create();
                case DLG_RESET_TO_DEFAULT:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.reset_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getOwner().resetToDefault();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
        }
    }

}

