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
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.model.Artboard;
import com.zhihu.android.app.mirror.model.Content;
import com.zhihu.android.app.mirror.model.Handshake;
import com.zhihu.android.app.mirror.model.MirrorInfo;
import com.zhihu.android.app.mirror.model.Page;
import com.zhihu.android.app.mirror.model.Screen;

public class MirrorUtils {
    public static final String MIRROR_FILTER = "Sketch Mirror";
    public static final String MIRROR_TYPE = "_http._tcp.";
    public static final int MIRROR_CONNECT_TIMEOUT = 60000; // ms
    public static final int MIRROR_READ_TIMEOUT = Integer.MAX_VALUE; // ms

    public static final String TYPE_DEVICE_INFO = "device-info";
    public static final String TYPE_CONNECTED = "connected";
    public static final String TYPE_MANIFEST = "manifest";
    public static final String TYPE_CURRENT_ARTBOARD = "current-artboard";
    public static final String TYPE_ARTBOARD = "artboard";
    public static final String TYPE_DISCONNECTED = "disconnected";

    private static final String BUILD_HANDSHAKE_TYPE = TYPE_DEVICE_INFO;
    private static final String BUILD_HANDSHAKE_CONTENT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36";
    private static final String BUILD_HANDSHAKE_CONTENT_DISPLAY_NAME = Build.MODEL;
    private static final String BUILD_HANDSHAKE_SCREEN_NAME = Build.MODEL;

    public static String buildMirrorHttpUrl(MirrorInfo mirrorInfo) {
        return "http://" + mirrorInfo.getHost() + ":" + mirrorInfo.getPort();
    }

    public static String buildMirrorWebSocketUrl(MirrorInfo mirrorInfo) {
        return "ws://" + mirrorInfo.getHost() + ":" + (mirrorInfo.getPort() + 1);
    }

    public static Handshake buildHandshake(Context context) {
        Screen screen = new Screen();
        screen.setName(BUILD_HANDSHAKE_SCREEN_NAME);
        screen.setWidth(DisplayUtils.getScreenWidth(context));
        screen.setHeight(DisplayUtils.getScreenHeight(context));
        screen.setScale(DisplayUtils.getScreenDensity(context)); // Math.round()

        String contentUuid = PreferenceUtils.getContentUuid(context);
        if (TextUtils.isEmpty(contentUuid)) {
            contentUuid = UUID.randomUUID().toString();
            PreferenceUtils.setContentUuid(context, contentUuid);
        }
        Content content = new Content();
        content.setUuid(contentUuid);
        content.setUserAgent(BUILD_HANDSHAKE_CONTENT_USER_AGENT);
        content.setDisplayName(BUILD_HANDSHAKE_CONTENT_DISPLAY_NAME);
        content.setScreens(new Screen[]{screen});

        String instanceId = PreferenceUtils.getInstanceId(context);
        if (TextUtils.isEmpty(instanceId)) {
            instanceId = UUID.randomUUID().toString();
            PreferenceUtils.setInstanceId(context, instanceId);
        }
        Handshake handshake = new Handshake();
        handshake.setType(BUILD_HANDSHAKE_TYPE);
        handshake.setInstanceId(instanceId);
        handshake.setContent(content);
        return handshake;
    }

    public static String buildArtboardPath(Context context, Artboard artboard) {
        return "/artboards/" + artboard.getId()
                + "@" + Math.round(DisplayUtils.getScreenDensity(context))
                + "x.png";
    }

    public static String buildArtboardHttpUrl(Context context, Artboard artboard) {
        return PreferenceUtils.getMirror(context) + artboard.getPath()
                + "?token=" + PreferenceUtils.getToken(context)
                + "&t=" + System.currentTimeMillis();
    }

    public static String getCurrentSketch(Context context, MirrorInfo mirrorInfo) {
        return !TextUtils.isEmpty(mirrorInfo.getName()) ? mirrorInfo.getName()
                : context.getString(R.string.connect_status_default_sketch);
    }

    public static List<Artboard> getArtboardsFromContent(Context context, Content content) {
        List<Artboard> list = new ArrayList<>();

        for (Page page : content.getContents().getPages()) {
            list.addAll(Arrays.asList(page.getArtboards()));
        }

        for (Artboard artboard : list) {
            artboard.setPath(MirrorUtils.buildArtboardPath(context, artboard));
        }

        return list;
    }
}
