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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.zhihu.android.app.mirror.R;

public class NetworkUtils {
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static void setWifiEnabled(Context context, boolean enabled) {
        ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(enabled);
    }

    public static String getCurrentSSID(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info != null && !TextUtils.isEmpty(info.getSSID()) ? info.getSSID() : context.getString(R.string.connect_status_default_ssid);
    }
}
