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
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.oneplus.shit.settings.SliderControllerBase;

import android.content.res.Resources;
import android.os.Looper;
import android.view.Gravity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import com.oneplus.shit.settings.R;

public final class FlashlightController extends SliderControllerBase {

    public static final int ID = 2;

    private static final String TAG = "FlashlightController";
    public static final String PACKAGE_SYSTEMUI = "com.android.systemui";

    private static final int FLASHLIGHT_OFF = 20;
    private static final int FLASHLIGHT_ON = 21;
    private static final int FLASHLIGHT_BLINK = 22;

    private static final long BLINK_INTERVAL = 250L;

    private final CameraManager mCameraManager;

    private String mCameraId;
    private boolean mTorchEnabled = false;

    private PowerManager.WakeLock mWakeLock;
    private Toast toast;

    private final Handler mBlinkHandler = new Handler();
    private final Runnable mBlinkRunnble = new Runnable() {
        @Override
        public void run() {
            if (setTorchMode(!mTorchEnabled)) {
                mBlinkHandler.postDelayed(this, BLINK_INTERVAL);
            } else {
                mWakeLock.release();
            }
        }
    };

    public FlashlightController(Context context) {
        super(context);
        mCameraManager = context.getSystemService(CameraManager.class);
        PowerManager pm = context.getSystemService(PowerManager.class);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    @Override
    protected boolean processAction(int action) {
        Log.d(TAG, "slider action: " + action);
        boolean succeed;
        switch (action) {
            case FLASHLIGHT_OFF:
                succeed = setTorchMode(false);
                mBlinkHandler.removeCallbacksAndMessages(null);
      	        showToast(R.string.flash_off, Toast.LENGTH_SHORT, 350);
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
                return succeed;
            case FLASHLIGHT_ON:
                mCameraId = getCameraId();
                succeed = setTorchMode(true);
                mBlinkHandler.removeCallbacksAndMessages(null);
                showToast(R.string.flash_on, Toast.LENGTH_SHORT, 350);
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
                return succeed;
            case FLASHLIGHT_BLINK:
                mBlinkHandler.removeCallbacksAndMessages(null);
                mCameraId = getCameraId();
                showToast(R.string.flash_blink, Toast.LENGTH_SHORT, 350);
                if (setTorchMode(true)) {
                    mWakeLock.acquire();
                    mBlinkHandler.postDelayed(mBlinkRunnble, BLINK_INTERVAL);
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    @Override
    public void reset() {
        setTorchMode(false);
        mBlinkHandler.removeCallbacksAndMessages(null);
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private boolean setTorchMode(boolean enabled) {
        if (mCameraId == null) {
            Log.e(TAG, "Camera is not available");
            return false;
        }

        try {
            mCameraManager.setTorchMode(mCameraId, enabled);
            mTorchEnabled = enabled;
            return true;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to turn flashlight " + (enabled ? "on" : "off"), e);
            return false;
        }
    }

    private String getCameraId() {
        try {
            for (final String cameraId : mCameraManager.getCameraIdList()) {
                final CameraCharacteristics characteristics =
                        mCameraManager.getCameraCharacteristics(cameraId);
                final boolean hasFlash = characteristics.get(
                        CameraCharacteristics.FLASH_INFO_AVAILABLE);
                final int facing = characteristics.get(
                        CameraCharacteristics.LENS_FACING);
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId;
                }
            }
            Log.e(TAG, "No camera with flashlight found");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to get camera", e);
        }

        return null;
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
}

