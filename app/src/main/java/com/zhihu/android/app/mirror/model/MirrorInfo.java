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

package com.zhihu.android.app.mirror.model;

import android.net.nsd.NsdServiceInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MirrorInfo {
    private String mName;
    private String mHost;
    private int mPort;
    private NsdServiceInfo mNsdServiceInfo;

    public MirrorInfo(NsdServiceInfo nsdServiceInfo) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(nsdServiceInfo.getServiceName());

        mName = "";
        while (matcher.find()) {
            mName = matcher.group(1);
        }

        mHost = "";
        if (nsdServiceInfo.getHost() != null) {
            mHost = nsdServiceInfo.getHost().getHostAddress();
        }

        mPort = nsdServiceInfo.getPort();
        mNsdServiceInfo = nsdServiceInfo;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public NsdServiceInfo getNsdServiceInfo() {
        return mNsdServiceInfo;
    }

    public void setNsdServiceInfo(NsdServiceInfo nsdServiceInfo) {
        mNsdServiceInfo = nsdServiceInfo;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MirrorInfo)) {
            return false;
        }

        if (this == object) {
            return true;
        }

        MirrorInfo info = (MirrorInfo) object;
        // return TextUtils.equals(mName, info.getName()) && TextUtils.equals(mHost, info.getHost()) && mPort == info.getPort();
        return mNsdServiceInfo.equals(info.getNsdServiceInfo());
    }
}
