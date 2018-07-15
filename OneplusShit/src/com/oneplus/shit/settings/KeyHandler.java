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
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.os.DeviceKeyHandler;

import java.util.Arrays;

import com.oneplus.shit.settings.SliderControllerBase;
import com.oneplus.shit.settings.slider.NotificationController;
import com.oneplus.shit.settings.slider.FlashlightController;
import com.oneplus.shit.settings.slider.BrightnessController;
import com.oneplus.shit.settings.slider.RotationController;
import com.oneplus.shit.settings.slider.RingerController;
import com.oneplus.shit.settings.slider.CaffeineController;

public class KeyHandler implements DeviceKeyHandler {

    private static final String TAG = "KeyHandler";

    private final Context mContext;

    private final NotificationController mNotificationController;
    private final FlashlightController mFlashlightController;
    private final BrightnessController mBrightnessController;
    private final RotationController mRotationController;
    private final RingerController mRingerController;
    private final CaffeineController mCaffeineController;

    private SliderControllerBase mSliderController;

    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int usage = intent.getIntExtra(ButtonConstants.EXTRA_SLIDER_USAGE, 0);
            int[] actions = intent.getIntArrayExtra(ButtonConstants.EXTRA_SLIDER_ACTIONS);
            handleSliderUpdate(usage, actions);
        }
    };

    public KeyHandler(Context context) {
        mContext = context;

        mNotificationController = new NotificationController(context);
        mFlashlightController = new FlashlightController(context);
        mBrightnessController = new BrightnessController(context);
        mRotationController = new RotationController(context);
        mRingerController = new RingerController(context);
        mCaffeineController = new CaffeineController(context);

        mContext.registerReceiver(mUpdateReceiver,
                new IntentFilter(ButtonConstants.ACTION_UPDATE_SLIDER_SETTINGS));
    }

    private boolean hasSetupCompleted() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
    }

    private void handleSliderUpdate(int usage, int[] actions) {
        Log.d(TAG, "update usage " + usage + " with actions " +
                Arrays.toString(actions));

        if (mSliderController != null) {
            mSliderController.reset();
        }

        switch (usage) {
            case NotificationController.ID:
                mSliderController = mNotificationController;
                break;
            case FlashlightController.ID:
                mSliderController = mFlashlightController;
                break;
            case BrightnessController.ID:
                mSliderController = mBrightnessController;
                break;
            case RotationController.ID:
                mSliderController = mRotationController;
                break;
            case RingerController.ID:
                mSliderController = mRingerController;
                break;
            case CaffeineController.ID:
                mSliderController = mCaffeineController;
                break;
        }

        if (mSliderController != null) {
            mSliderController.update(actions);
            mSliderController.restoreState();
        }
    }

    public KeyEvent handleKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return event;
        }

        int scanCode = event.getScanCode();
        if (mSliderController != null &&
                mSliderController.processEvent(scanCode)) {
            return null;
        }

        return event;
    }

}
