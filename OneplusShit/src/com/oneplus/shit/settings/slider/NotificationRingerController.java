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

import android.content.res.Resources;
import android.os.Looper;
import android.view.Gravity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;
import android.app.ActivityThread;

import com.oneplus.shit.settings.R;

public final class NotificationRingerController extends SliderControllerBase {

    public static final int ID = 6;

    private static final String TAG = "NotificationRingerController";
    public static final String PACKAGE_SYSTEMUI = "com.android.systemui";

    private static final int NOTIFICATION_TOTAL_SILENCE = 70;
    private static final int NOTIFICATION_PRIORITY_ONLY = 72;
    private static final int NOTIFICATION_ALL = 73;
    private static final int RINGER_VIBRATE = 74;
    private static final int RINGER_SILENT = 75;
    private static final int CHANGE_DELAY = 100;

    private final NotificationManager mNotificationManager;
    private final AudioManager mAudioManager;
    private Handler mHandler;
    private int mRingMode;
    private int mZenMode;
    private Toast toast;
    private final Context mSysUiContext;
    private final Context mResContext;

    public NotificationRingerController(Context context) {
        super(context);
        mHandler = new Handler();
        mNotificationManager = context.getSystemService(NotificationManager.class);
        mAudioManager = context.getSystemService(AudioManager.class);
        mSysUiContext = ActivityThread.currentActivityThread().getSystemUiContext();
        mResContext = getPackageContext(mContext, "com.oneplus.shit.settings");
    }

    @Override
    protected boolean processAction(int action) {
        Log.d(TAG, "slider action: " + action);

        switch (action) {
            case RINGER_VIBRATE:
                mRingMode = RINGER_VIBRATE;
                mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG);
       	        showToast(R.string.toast_ringer_vibrate, Toast.LENGTH_SHORT, 350);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mRingMode != RINGER_VIBRATE) return;
                        mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
                    }
                }, CHANGE_DELAY);
                return true;
            case RINGER_SILENT:
                mRingMode = RINGER_SILENT;
                mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG);
                showToast(R.string.toast_ringer_silent, Toast.LENGTH_SHORT, 350);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mRingMode != RINGER_SILENT) return;
                        mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
                    }
                }, CHANGE_DELAY);
                return true;
            case NOTIFICATION_TOTAL_SILENCE:
                mZenMode = NOTIFICATION_TOTAL_SILENCE;
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
                showToast(R.string.toast_total_silence, Toast.LENGTH_SHORT, 350);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mZenMode != NOTIFICATION_TOTAL_SILENCE) return;
                        mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_NO_INTERRUPTIONS, null, TAG);
                    }
                }, CHANGE_DELAY);
                return true;
            case NOTIFICATION_PRIORITY_ONLY:
                mZenMode = NOTIFICATION_PRIORITY_ONLY;
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
                showToast(R.string.toast_priority_only, Toast.LENGTH_SHORT, 350);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mZenMode != NOTIFICATION_PRIORITY_ONLY) return;
                        mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS, null, TAG);
                    }
                }, CHANGE_DELAY);
                return true;
            case NOTIFICATION_ALL:
                mRingMode = NOTIFICATION_ALL;
                mNotificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG);
                showToast(R.string.toast_ringer_normal, Toast.LENGTH_SHORT, 350);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mRingMode != NOTIFICATION_ALL) return;
                        mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
                    }
                }, CHANGE_DELAY);
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

    void showToast(int messageId, int duration, int yOffset) {
        final String message = mResContext.getResources().getString(messageId);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
        @Override
        public void run() {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(mSysUiContext, message, duration);
            toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, yOffset);
            toast.show();
            }
        });
    }

    public static Context getPackageContext(Context context, String packageName) {
        Context pkgContext = null;
        if (context.getPackageName().equals(packageName)) {
            pkgContext = context;
        } else {
            try {
                pkgContext = context.createPackageContext(packageName,
                        Context.CONTEXT_IGNORE_SECURITY
                                | Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pkgContext;
    }
}

