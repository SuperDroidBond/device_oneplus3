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

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.view.IWindowManager;
import android.view.Surface;
import android.view.WindowManagerGlobal;

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

public final class RotationController extends SliderControllerBase {

    public static final int ID = 4;

    private static final String TAG = "RotationController";
    public static final String PACKAGE_SYSTEMUI = "com.android.systemui";

    private static final int ROTATION_AUTO = 40;
    private static final int ROTATION_0 = 41;
    private static final int ROTATION_90 = 42;
    private static final int ROTATION_270 = 43;

    private Toast toast;
    private final Context mSysUiContext;
    private final Context mResContext;

    public RotationController(Context context) {
        super(context);
        mSysUiContext = ActivityThread.currentActivityThread().getSystemUiContext();
        mResContext = getPackageContext(mContext, "com.oneplus.shit.settings");
    }

    @Override
    protected boolean processAction(int action) {
        Log.d(TAG, "slider action: " + action);
        switch (action) {
            case ROTATION_AUTO:
            showToast(R.string.toast_rot_auto, Toast.LENGTH_SHORT, 350);
                return setRotation(false, 0);
            case ROTATION_0:
            showToast(R.string.toast_rot_0, Toast.LENGTH_SHORT, 350);
                return setRotation(true, Surface.ROTATION_0);
            case ROTATION_90:
            showToast(R.string.toast_rot_90, Toast.LENGTH_SHORT, 350);
                return setRotation(true, Surface.ROTATION_90);
            case ROTATION_270:
            showToast(R.string.toast_rot_270, Toast.LENGTH_SHORT, 350);
                return setRotation(true, Surface.ROTATION_270);
            default:
                return false;
        }
    }

    @Override
    public void reset() {
        setRotation(false, 0);
    }

    private boolean setRotation(boolean locked, int rotation) {
        try {
            IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
            if (locked) {
                wm.freezeRotation(rotation);
            } else {
                wm.thawRotation();
            }
            return true;
        } catch (RemoteException exc) {
            Log.w(TAG, "Unable to save auto-rotate setting");
            return false;
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
