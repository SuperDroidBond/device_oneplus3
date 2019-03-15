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

package com.oneplus.shit.settings.slider;

import android.app.NotificationManager;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseIntArray;

import com.oneplus.shit.settings.SliderControllerBase;

public final class NotificationController extends SliderControllerBase {

    public static final int ID = 1;

    private static final String TAG = "NotificationController";

    private static final int NOTIFICATION_TOTAL_SILENCE = 10;
    private static final int NOTIFICATION_ALARMS_ONLY = 11;
    private static final int NOTIFICATION_PRIORITY_ONLY = 12;
    private static final int NOTIFICATION_ALL = 13;

    private static final SparseIntArray MODES = new SparseIntArray();
    static {
        MODES.put(NOTIFICATION_TOTAL_SILENCE,
                Settings.Global.ZEN_MODE_NO_INTERRUPTIONS);
        MODES.put(NOTIFICATION_ALARMS_ONLY,
                Settings.Global.ZEN_MODE_ALARMS);
        MODES.put(NOTIFICATION_PRIORITY_ONLY,
                Settings.Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS);
        MODES.put(NOTIFICATION_ALL,
                Settings.Global.ZEN_MODE_OFF);
    }

    private final NotificationManager mNotificationManager;

    public NotificationController(Context context) {
        super(context);
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected boolean processAction(int action) {
        Log.d(TAG, "slider action: " + action);
        if (MODES.indexOfKey(action) >= 0) {
            mNotificationManager.setZenMode(MODES.get(action), null, TAG);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset() {
        mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG);
    }
}
