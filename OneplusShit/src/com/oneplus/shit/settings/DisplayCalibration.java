/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.oneplus.shit.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.Matrix;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.SparseArray;
import android.util.Slog;
import android.view.Menu;
import android.view.MenuInflater;
import com.android.internal.app.ColorDisplayController;

import java.util.List;
import java.util.Arrays;

import android.app.ActionBar;
import com.oneplus.shit.settings.utils.SeekBarPreference;
import com.oneplus.shit.settings.R;

public class DisplayCalibration extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "DisplayCalibration";
    public static final String KEY_KCAL_ENABLED = "kcal_enabled";
    public static final String KEY_KCAL_RED = "kcal_red";
    public static final String KEY_KCAL_GREEN = "kcal_green";
    public static final String KEY_KCAL_BLUE = "kcal_blue";
    public static final String KEY_KCAL_SATURATION = "kcal_saturation";
    public static final String KEY_KCAL_CONTRAST = "kcal_contrast";
    public static final String KEY_KCAL_COLOR_TEMP = "kcal_color_temp";
    private static final String PREF_READING_MODE = "reading_mode";

    private static final int LEVEL_COLOR_MATRIX_READING = 201;

    private static final SparseArray<float[]> mColorMatrix = new SparseArray<>(3);
    private static final int SURFACE_FLINGER_TRANSACTION_COLOR_MATRIX = 1015;
    private static final float[][] mTempColorMatrix = new float[2][16];

    private SeekBarPreference mKcalRed;
    private SeekBarPreference mKcalBlue;
    private SeekBarPreference mKcalGreen;
    private SeekBarPreference mKcalSaturation;
    private SeekBarPreference mKcalContrast;
    private SeekBarPreference mKcalColorTemp;
    private SharedPreferences mPrefs;
    private SwitchPreference mKcalEnabled;
    private SwitchPreference mReadingMode;
    private boolean mEnabled;

    private String mRed;
    private String mBlue;
    private String mGreen;

    private static final String COLOR_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal";
    private static final String COLOR_FILE_CONTRAST = "/sys/devices/platform/kcal_ctrl.0/kcal_cont";
    private static final String COLOR_FILE_SATURATION = "/sys/devices/platform/kcal_ctrl.0/kcal_sat";
    private static final String COLOR_FILE_ENABLE = "/sys/devices/platform/kcal_ctrl.0/kcal_enable";

    /**
     * Matrix and offset used for converting color to grayscale.
     * Copied from com.android.server.accessibility.DisplayAdjustmentUtils.MATRIX_GRAYSCALE
     */
    private static final float[] MATRIX_GRAYSCALE = new float[] {
            .2126f, .2126f, .2126f, 0,
            .7152f, .7152f, .7152f, 0,
            .0722f, .0722f, .0722f, 0,
            0,      0,      0, 1
    };

    /** Full color matrix and offset */
    private static final float[] MATRIX_NORMAL = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.display_cal);

        ImageView imageView = (ImageView) findViewById(R.id.calibration_pic);
        imageView.setImageResource(R.drawable.calibration_png);

        addPreferencesFromResource(R.xml.display_calibration);

        mKcalEnabled = (SwitchPreference) findPreference(KEY_KCAL_ENABLED);
        mKcalEnabled.setChecked(mPrefs.getBoolean(DisplayCalibration.KEY_KCAL_ENABLED, false));
        mKcalEnabled.setOnPreferenceChangeListener(this);

        mKcalRed = (SeekBarPreference) findPreference(KEY_KCAL_RED);
        mKcalRed.setInitValue(mPrefs.getInt(KEY_KCAL_RED, mKcalRed.def));
        mKcalRed.setOnPreferenceChangeListener(this);

        mKcalGreen = (SeekBarPreference) findPreference(KEY_KCAL_GREEN);
        mKcalGreen.setInitValue(mPrefs.getInt(KEY_KCAL_GREEN, mKcalGreen.def));
        mKcalGreen.setOnPreferenceChangeListener(this);

        mKcalBlue = (SeekBarPreference) findPreference(KEY_KCAL_BLUE);
        mKcalBlue.setInitValue(mPrefs.getInt(KEY_KCAL_BLUE, mKcalBlue.def));
        mKcalBlue.setOnPreferenceChangeListener(this);

        mKcalSaturation = (SeekBarPreference) findPreference(KEY_KCAL_SATURATION);
        mKcalSaturation.setInitValue(mPrefs.getInt(KEY_KCAL_SATURATION, mKcalSaturation.def));
        mKcalSaturation.setOnPreferenceChangeListener(this);

        mKcalContrast = (SeekBarPreference) findPreference(KEY_KCAL_CONTRAST);
        mKcalContrast.setInitValue(mPrefs.getInt(KEY_KCAL_CONTRAST, mKcalContrast.def));
        mKcalContrast.setOnPreferenceChangeListener(this);

        mKcalColorTemp = (SeekBarPreference) findPreference(KEY_KCAL_COLOR_TEMP);
        mKcalColorTemp.setInitValue(mPrefs.getInt(KEY_KCAL_COLOR_TEMP, mKcalColorTemp.def));
        mKcalColorTemp.setOnPreferenceChangeListener(this);

        mRed = String.valueOf(mPrefs.getInt(KEY_KCAL_RED, mKcalRed.def));
        mGreen = String.valueOf(mPrefs.getInt(KEY_KCAL_GREEN, mKcalGreen.def));
        mBlue = String.valueOf(mPrefs.getInt(KEY_KCAL_BLUE, mKcalBlue.def));

        mReadingMode = (SwitchPreference) findPreference(PREF_READING_MODE);
    }

    private boolean isSupported(String file) {
        return UtilsKCAL.fileWritable(file);
    }

    public static void restore(Context context) {
       boolean storeEnabled = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(DisplayCalibration.KEY_KCAL_ENABLED, false);
       if (storeEnabled) {
           UtilsKCAL.writeValue(COLOR_FILE_ENABLE, "1");
           UtilsKCAL.writeValue(COLOR_FILE, "1");
           int storedRed = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_RED, 256);
           int storedGreen = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_GREEN, 256);
           int storedBlue = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_BLUE, 256);
           int storedSaturation = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_SATURATION, 255);
           int storedContrast = PreferenceManager
                   .getDefaultSharedPreferences(context).getInt(DisplayCalibration.KEY_KCAL_CONTRAST, 255);
           String storedValue = ((String) String.valueOf(storedRed)
                   + " " + String.valueOf(storedGreen) + " " +  String.valueOf(storedBlue));
           UtilsKCAL.writeValue(COLOR_FILE, storedValue);
           UtilsKCAL.writeValue(COLOR_FILE_CONTRAST, String.valueOf(storedContrast));
           UtilsKCAL.writeValue(COLOR_FILE_SATURATION, String.valueOf(storedSaturation));
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.kcal_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_reset:
                reset();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void reset() {
        int red = mKcalRed.reset();
        int green = mKcalGreen.reset();
        int blue = mKcalBlue.reset();
        int saturation = mKcalSaturation.reset();
        int contrast = mKcalContrast.reset();

        mPrefs.edit().putInt(KEY_KCAL_RED, red).commit();
        mPrefs.edit().putInt(KEY_KCAL_GREEN, green).commit();
        mPrefs.edit().putInt(KEY_KCAL_BLUE, blue).commit();
        mPrefs.edit().putInt(KEY_KCAL_SATURATION, saturation).commit();
        mPrefs.edit().putInt(KEY_KCAL_CONTRAST, contrast).commit();

        String storedValue = Integer.toString(red) + " " + Integer.toString(green) + " " +  Integer.toString(blue);

        UtilsKCAL.writeValue(COLOR_FILE, storedValue);
        UtilsKCAL.writeValue(COLOR_FILE_SATURATION, Integer.toString(saturation));
        UtilsKCAL.writeValue(COLOR_FILE_CONTRAST, Integer.toString(contrast));

        int cct = UtilsKCAL.KfromRGB(mPrefs.getInt(KEY_KCAL_RED, 256), mPrefs.getInt(KEY_KCAL_GREEN, 256), mPrefs.getInt(KEY_KCAL_BLUE, 256));
        mKcalColorTemp.setValue(cct);
    }
 
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mReadingMode) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            setReadingMode(checked);
            return true;
        }
        return onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mKcalEnabled) {
            Boolean enabled = (Boolean) newValue;
            mPrefs.edit().putBoolean(KEY_KCAL_ENABLED, enabled).commit();
            mRed = String.valueOf(mPrefs.getInt(KEY_KCAL_RED, 256));
            mBlue = String.valueOf(mPrefs.getInt(KEY_KCAL_BLUE, 256));
            mGreen = String.valueOf(mPrefs.getInt(KEY_KCAL_GREEN, 256));
            String storedValue = ((String) String.valueOf(mRed)
                   + " " + String.valueOf(mGreen) + " " +  String.valueOf(mBlue));
            String mSaturation = String.valueOf(mPrefs.getInt(KEY_KCAL_SATURATION, 256));
            String mContrast = String.valueOf(mPrefs.getInt(KEY_KCAL_CONTRAST, 256));
            UtilsKCAL.writeValue(COLOR_FILE_ENABLE, enabled ? "1" : "0");
            UtilsKCAL.writeValue(COLOR_FILE, storedValue);
            UtilsKCAL.writeValue(COLOR_FILE_SATURATION, mSaturation);
            UtilsKCAL.writeValue(COLOR_FILE_CONTRAST, mContrast);

            int cct = UtilsKCAL.KfromRGB(mPrefs.getInt(KEY_KCAL_RED, 256), mPrefs.getInt(KEY_KCAL_GREEN, 256), mPrefs.getInt(KEY_KCAL_BLUE, 256));
            mKcalColorTemp.setValue(cct);
            return true;
        } else if (preference == mKcalRed) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_RED, (int) val).commit();
            mGreen = String.valueOf(mPrefs.getInt(KEY_KCAL_GREEN, 256));
            mBlue = String.valueOf(mPrefs.getInt(KEY_KCAL_BLUE, 256));
            String strVal = ((String) newValue + " " + mGreen + " " +mBlue);
            UtilsKCAL.writeValue(COLOR_FILE, strVal);

            int cct = UtilsKCAL.KfromRGB(val, mPrefs.getInt(KEY_KCAL_GREEN, 256), mPrefs.getInt(KEY_KCAL_BLUE, 256));
            mKcalColorTemp.setValue(cct);
            return true;
        } else if (preference == mKcalGreen) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_GREEN, (int) val).commit();
            mRed = String.valueOf(mPrefs.getInt(KEY_KCAL_RED, 256));
            mBlue = String.valueOf(mPrefs.getInt(KEY_KCAL_BLUE, 256));
            String strVal = ((String) mRed + " " + newValue + " " +mBlue);
            UtilsKCAL.writeValue(COLOR_FILE, strVal);

            int cct = UtilsKCAL.KfromRGB(mPrefs.getInt(KEY_KCAL_RED, 256), val, mPrefs.getInt(KEY_KCAL_BLUE, 256));
            mKcalColorTemp.setValue(cct);
            return true;
        } else if (preference == mKcalBlue) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_BLUE, (int) val).commit();
            mRed = String.valueOf(mPrefs.getInt(KEY_KCAL_RED, 256));
            mGreen = String.valueOf(mPrefs.getInt(KEY_KCAL_GREEN, 256));
            String strVal = ((String) mRed + " " + mGreen + " " +newValue);
            UtilsKCAL.writeValue(COLOR_FILE, strVal);

            int cct = UtilsKCAL.KfromRGB(mPrefs.getInt(KEY_KCAL_RED, 256), mPrefs.getInt(KEY_KCAL_GREEN, 256), val);
            mKcalColorTemp.setValue(cct);
            return true;
        } else if (preference == mKcalSaturation) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_SATURATION, (int) val).commit();
            String strVal = (String) newValue;
            UtilsKCAL.writeValue(COLOR_FILE_SATURATION, strVal);
            return true;
        } else if (preference == mKcalContrast) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_KCAL_CONTRAST, (int) val).commit();
            String strVal = (String) newValue;
            UtilsKCAL.writeValue(COLOR_FILE_CONTRAST, strVal);
            return true;
        } else if (preference == mKcalColorTemp) {
            int val = Integer.parseInt((String) newValue);
            int[] colorTemp = UtilsKCAL.RGBfromK(val);
            int red = colorTemp[0];
            int green = colorTemp[1];
            int blue = colorTemp[2];

            mKcalRed.setValue(red);
            mKcalGreen.setValue(green);
            mKcalBlue.setValue(blue);

            mPrefs.edit().putInt(KEY_KCAL_RED, red).commit();
            mPrefs.edit().putInt(KEY_KCAL_GREEN, green).commit();
            mPrefs.edit().putInt(KEY_KCAL_BLUE, blue).commit();

            String storedValue = Integer.toString(red) + " " + Integer.toString(green) + " " +  Integer.toString(blue);
            UtilsKCAL.writeValue(COLOR_FILE, storedValue);
        }
        return false;
    }

    public static void setReadingMode(boolean state) {
         setColorMatrix(LEVEL_COLOR_MATRIX_READING,
              state ? MATRIX_GRAYSCALE : MATRIX_NORMAL);
    }

    public static void setColorMatrix(int level, float[] value) {
        if (value != null && value.length != 16) {
            throw new IllegalArgumentException("Expected length: 16 (4x4 matrix)"
                    + ", actual length: " + value.length);
        }

        synchronized (mColorMatrix) {
            final float[] oldValue = mColorMatrix.get(level);
            if (!Arrays.equals(oldValue, value)) {
                if (value == null) {
                    mColorMatrix.remove(level);
                } else if (oldValue == null) {
                    mColorMatrix.put(level, Arrays.copyOf(value, value.length));
                } else {
                    System.arraycopy(value, 0, oldValue, 0, value.length);
                }

                // Update the current color transform.
                applyColorMatrix(computeColorMatrixLocked());
            }
        }
    }

    private static void applyColorMatrix(float[] m) {
        final IBinder flinger = ServiceManager.getService("SurfaceFlinger");
        if (flinger != null) {
            final Parcel data = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            if (m != null) {
                data.writeInt(1);
                for (int i = 0; i < 16; i++) {
                    data.writeFloat(m[i]);
                }
            } else {
                data.writeInt(0);
            }
            try {
                flinger.transact(SURFACE_FLINGER_TRANSACTION_COLOR_MATRIX, data, null, 0);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set color transform", ex);
            } finally {
                data.recycle();
            }
        }
    }

    private static float[] computeColorMatrixLocked() {
        final int count = mColorMatrix.size();
        if (count == 0) {
            return null;
        }

        final float[][] result = mTempColorMatrix;
        Matrix.setIdentityM(result[0], 0);
        for (int i = 0; i < count; i++) {
            float[] rhs = mColorMatrix.valueAt(i);
            Matrix.multiplyMM(result[(i + 1) % 2], 0, result[i % 2], 0, rhs, 0);
        }
        return result[count % 2];
    }
}

