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
import android.os.Vibrator;
import android.util.Log;

import com.oneplus.shit.settings.utils.FileUtils;

public abstract class SliderControllerBase {

    private static final String TAG = "SliderControllerBase";

    protected final Context mContext;

    private Vibrator mVibrator;

    private int[] mActions = null;

    public SliderControllerBase(Context context) {
        mContext = context;
        mVibrator = context.getSystemService(Vibrator.class);
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            mVibrator = null;
        }
    }

    public final void update(int[] actions) {
        if (actions != null && actions.length == 3) {
            mActions = actions;
        }
    }

    public final boolean isSupported(int key) {
        return key == ButtonConstants.KEY_SLIDER_TOP ||
                key == ButtonConstants.KEY_SLIDER_MIDDLE ||
                key == ButtonConstants.KEY_SLIDER_BOTTOM;
    }

    protected abstract boolean processAction(int action);

    public final boolean processEvent(int key) {
        if (mActions == null) {
            return false;
        }

        boolean processed = false;
        switch (key) {
            case ButtonConstants.KEY_SLIDER_TOP:
                processed = processAction(mActions[0]);
                break;
            case ButtonConstants.KEY_SLIDER_MIDDLE:
                processed = processAction(mActions[1]);
                break;
            case ButtonConstants.KEY_SLIDER_BOTTOM:
                processed = processAction(mActions[2]);
                break;
        }

        if (processed) {
            doHapticFeedback();
        }

        return processed;
    }

    public abstract void reset();

    public final void restoreState() {
        if (mActions == null) {
            return;
        }

        try {
            int state = Integer.parseInt(FileUtils.readOneLine(
                    ButtonConstants.SLIDER_STATE_NODE));
            processAction(mActions[state - 1]);
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore slider state", e);
        }
    }

    private void doHapticFeedback() {
        if (mVibrator == null) {
            return;
        }
            mVibrator.vibrate(50);
    }

    protected final <T> T getSystemService(String name) {
        return (T) mContext.getSystemService(name);
    }
}
