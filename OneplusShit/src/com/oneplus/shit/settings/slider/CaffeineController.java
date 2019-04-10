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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

import static java.util.concurrent.TimeUnit.MINUTES;

import com.oneplus.shit.settings.SliderControllerBase;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;
import android.app.ActivityThread;

import com.oneplus.shit.settings.R;

public final class CaffeineController extends SliderControllerBase {

    public static final int ID = 7;

    private static final String TAG = "CaffeineController";
    public static final String PACKAGE_SYSTEMUI = "com.android.systemui";

    private static final int CAFFEINE_OFF = 60;
    private static final int CAFFEINE_5MIN = 61;
    private static final int CAFFEINE_10MIN = 62;
    private static final int CAFFEINE_30MIN = 63;
    private static final int CAFFEINE_INFINITY = 64;

    private PowerManager.WakeLock mWakeLock;
    private ScreenStateReceiver mScreenStateReceiver;
    private final Context mSysUiContext;
    private final Context mResContext;

    private int lastAction = CAFFEINE_OFF;
    private Toast toast;

    public CaffeineController(Context context) {
        super(context);
        PowerManager pm = getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
        mScreenStateReceiver = new ScreenStateReceiver();
        mSysUiContext = ActivityThread.currentActivityThread().getSystemUiContext();
        mResContext = getPackageContext(mContext, "com.oneplus.shit.settings");
    }

    @Override
    protected boolean processAction(int action) {
        Log.d(TAG, "slider action: " + action);

        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        lastAction = action;

        switch (action) {
            case CAFFEINE_OFF:
                mScreenStateReceiver.unregister();
       	        showToast(R.string.toast_caffine_off, Toast.LENGTH_SHORT, 350);
                return true;
            case CAFFEINE_5MIN:
                mWakeLock.acquire(MINUTES.toMillis(5));
                mScreenStateReceiver.register();
                showToast(R.string.toast_caffine_5min, Toast.LENGTH_SHORT, 350);
                return true;
            case CAFFEINE_10MIN:
                mWakeLock.acquire(MINUTES.toMillis(10));
                mScreenStateReceiver.register();
                showToast(R.string.toast_caffine_10min, Toast.LENGTH_SHORT, 350);
                return true;
            case CAFFEINE_30MIN:
                mWakeLock.acquire(MINUTES.toMillis(30));
                mScreenStateReceiver.register();
                showToast(R.string.toast_caffine_30min, Toast.LENGTH_SHORT, 350);
                return true;
            case CAFFEINE_INFINITY:
                mWakeLock.acquire();
                mScreenStateReceiver.register();
                showToast(R.string.toast_caffine_infinity, Toast.LENGTH_SHORT, 350);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void reset() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        mScreenStateReceiver.unregister();
    }

    private final class ScreenStateReceiver extends BroadcastReceiver {

        private boolean mRegistered = false;

        void register() {
            if (!mRegistered) {
                final IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                mContext.registerReceiver(this, filter);
                mRegistered = true;
            }
        }

        void unregister() {
            if (mRegistered) {
                mContext.unregisterReceiver(this);
                mRegistered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_ON:
                    processAction(lastAction);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    if (mWakeLock.isHeld()) {
                        mWakeLock.release();
                    }
                    break;
            }
        }
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
