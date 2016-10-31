/*
 * Mirror - Yet another Sketch Mirror App for Android.
 * Copyright (C) 2016 Zhihu Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zhihu.android.app.mirror.util;

import android.content.Context;
import android.preference.PreferenceManager;

public class PreferenceUtils {
    private static final String MIRROR = "MIRROR";
    private static final String CONTENT_UUID = "CONTENT_UUID";
    private static final String INSTANCE_ID = "INSTANCE_ID";
    private static final String TOKEN = "TOKEN";
    private static final String DEVICE = "DEVICE";

    public static String getMirror(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(MIRROR, null);
    }

    public static void setMirror(Context context, String mirror) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(MIRROR, mirror).apply();
    }

    public static String getContentUuid(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(CONTENT_UUID, null);
    }

    public static void setContentUuid(Context context, String contentUuid) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(CONTENT_UUID, contentUuid).apply();
    }

    public static String getInstanceId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(INSTANCE_ID, null);
    }

    public static void setInstanceId(Context context, String instanceId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(INSTANCE_ID, instanceId).apply();
    }

    public static String getToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(TOKEN, null);
    }

    public static void setToken(Context context, String token) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(TOKEN, token).apply();
    }

    public static String getDevice(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(DEVICE, null);
    }

    public static void setDevice(Context context, String device) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(DEVICE, device).apply();
    }
}
