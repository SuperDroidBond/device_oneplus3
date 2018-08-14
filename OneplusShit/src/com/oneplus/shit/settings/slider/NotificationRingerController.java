/*
 * Copyright (C) 2018 crDroid Android Project
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
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.oneplus.shit.settings.SliderControllerBase;

public final class NotificationRingerController extends SliderControllerBase {

    public static final int ID = 6;

    private static final String TAG = "NotificationRingerController";

    private static final int NOTIFICATION_TOTAL_SILENCE = 70;
    private static final int NOTIFICATION_ALARMS_ONLY = 71;
    private static final int NOTIFICATION_PRIORITY_ONLY = 72;
    private static final int NOTIFICATION_ALL = 73;
    private static final int RINGER_VIBRATE = 74;
    private static final int RINGER_SILENT = 75;
    private static final int CHANGE_DELAY = 100;

    private final NotificationManager mNotificationManager;
    private final AudioManager mAudioManager;
    private Handler mHandler;
    private int mCurrentRinger;
    private int mCurrentNotif;

    public NotificationRingerController(Context context) {
        super(context);
        mHandler = new Handler();
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE);
        mAudioManager = getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected boolean processAction(int action) {
        Log.d(TAG, "slider action: " + action);

        switch (action) {
            case RINGER_VIBRATE:
                mCurrentRinger = RINGER_VIBRATE;
                mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentRinger != RINGER_VIBRATE) return;
                        mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
                    }
                }, CHANGE_DELAY);
                return true;
            case RINGER_SILENT:
                mCurrentRinger = RINGER_SILENT;
                mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentRinger != RINGER_SILENT) return;
                        mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
                    }
                }, CHANGE_DELAY);
                return true;
            case NOTIFICATION_TOTAL_SILENCE:
                mCurrentNotif = NOTIFICATION_TOTAL_SILENCE;
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentNotif != NOTIFICATION_TOTAL_SILENCE) return;
                        mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_NO_INTERRUPTIONS, null, TAG);
                    }
                }, CHANGE_DELAY);
                return true;
            case NOTIFICATION_ALARMS_ONLY:
                mCurrentNotif = NOTIFICATION_ALARMS_ONLY;
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentNotif != NOTIFICATION_ALARMS_ONLY) return;
                        mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_ALARMS, null, TAG);
                    }
                }, CHANGE_DELAY);
                return true;
            case NOTIFICATION_PRIORITY_ONLY:
                mCurrentNotif = NOTIFICATION_PRIORITY_ONLY;
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentNotif != NOTIFICATION_PRIORITY_ONLY) return;
                        mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS, null, TAG);
                    }
                }, CHANGE_DELAY);

                return true;
            case NOTIFICATION_ALL:
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
                mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void reset() {
        mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
        mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG);
    }
}


