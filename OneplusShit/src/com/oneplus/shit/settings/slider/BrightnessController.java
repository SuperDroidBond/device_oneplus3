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
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.oneplus.shit.settings.SliderControllerBase;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import com.oneplus.shit.settings.R;

public final class BrightnessController extends SliderControllerBase {

    public static final int ID = 3;

    private static final String TAG = "BrightnessController";
    public static final String PACKAGE_SYSTEMUI = "com.android.systemui";

    private static final int BRIGHTNESS_AUTO = 30;
    private static final int BRIGHTNESS_BRIGHTEST = 31;
    private static final int BRIGHTNESS_DARKEST = 32;

    private static final int DARKEST = 0;
    private static final int BRIGHTEST = 255;

    private Toast toast;

    public BrightnessController(Context context) {
        super(context);
    }

    @Override
    protected boolean processAction(int action) {
        Log.d(TAG, "slider action: " + action);
        switch (action) {
            case BRIGHTNESS_AUTO:
            showToast(R.string.toast_auto_brightness, Toast.LENGTH_SHORT, 350);
                return writeSettings(Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            case BRIGHTNESS_BRIGHTEST:
            showToast(R.string.toast_max_brightness, Toast.LENGTH_SHORT, 350);
                return writeSettings(Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) &&
                    writeSettings(Settings.System.SCREEN_BRIGHTNESS, BRIGHTEST);
            case BRIGHTNESS_DARKEST:
            showToast(R.string.toast_min_brightness, Toast.LENGTH_SHORT, 350);
                return writeSettings(Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) &&
                    writeSettings(Settings.System.SCREEN_BRIGHTNESS, DARKEST);
            default:
                return false;
        }
    }

    void showToast(int messageId, int duration, int yOffset) {
	Context resCtx = getPackageContext(mContext, "com.oneplus.shit.settings");
	final String message = resCtx.getResources().getString(messageId);
	Context ctx = getPackageContext(mContext, PACKAGE_SYSTEMUI);
	Handler handler = new Handler(Looper.getMainLooper());
	handler.post(new Runnable() {
	    @Override
	    public void run() {
		if (toast != null) toast.cancel();
		toast = Toast.makeText(ctx, message, duration);
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

    @Override
    public void reset() {
        writeSettings(Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    private boolean writeSettings(String key, int value) {
        return Settings.System.putIntForUser(mContext.getContentResolver(),
                    key, value, UserHandle.USER_CURRENT);
    }

}
