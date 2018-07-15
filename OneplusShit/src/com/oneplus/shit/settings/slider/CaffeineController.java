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

public final class CaffeineController extends SliderControllerBase {

    public static final int ID = 6;

    private static final String TAG = "CaffeineController";

    private static final int CAFFEINE_OFF = 60;
    private static final int CAFFEINE_5MIN = 61;
    private static final int CAFFEINE_10MIN = 62;
    private static final int CAFFEINE_30MIN = 63;
    private static final int CAFFEINE_INFINITY = 64;

    private PowerManager.WakeLock mWakeLock;
    private ScreenStateReceiver mScreenStateReceiver;

    private int lastAction = CAFFEINE_OFF;

    public CaffeineController(Context context) {
        super(context);
        PowerManager pm = getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
        mScreenStateReceiver = new ScreenStateReceiver();
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
                return true;
            case CAFFEINE_5MIN:
                mWakeLock.acquire(MINUTES.toMillis(5));
                mScreenStateReceiver.register();
                return true;
            case CAFFEINE_10MIN:
                mWakeLock.acquire(MINUTES.toMillis(10));
                mScreenStateReceiver.register();
                return true;
            case CAFFEINE_30MIN:
                mWakeLock.acquire(MINUTES.toMillis(30));
                mScreenStateReceiver.register();
                return true;
            case CAFFEINE_INFINITY:
                mWakeLock.acquire();
                mScreenStateReceiver.register();
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
}
