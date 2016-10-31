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

import com.google.gson.annotations.SerializedName;

public class Content {
    @SerializedName("type")
    private String mType;

    @SerializedName("display-name")
    private String mDisplayName;

    @SerializedName("screens")
    private Screen[] mScreens;

    @SerializedName("user-agent")
    private String mUserAgent;

    @SerializedName("uuid")
    private String mUuid;

    @SerializedName("token")
    private String mToken;

    @SerializedName("device")
    private String mDevice;

    @SerializedName("contents")
    private Contents mContents;

    @SerializedName("id")
    private String mId;

    @SerializedName("metadata")
    private Metadata mMetadata;

    @SerializedName("name")
    private String mName;

    @SerializedName("url")
    private String mUrl;

    @SerializedName("identifier")
    private String mIdentifier;

    @SerializedName("path")
    private String mPath;

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public Screen[] getScreens() {
        return mScreens;
    }

    public void setScreens(Screen[] screens) {
        mScreens = screens;
    }

    public String getUserAgent() {
        return mUserAgent;
    }

    public void setUserAgent(String userAgent) {
        mUserAgent = userAgent;
    }

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public String getDevice() {
        return mDevice;
    }

    public void setDevice(String device) {
        mDevice = device;
    }

    public Contents getContents() {
        return mContents;
    }

    public void setContents(Contents contents) {
        mContents = contents;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public Metadata getMetadata() {
        return mMetadata;
    }

    public void setMetadata(Metadata metadata) {
        mMetadata = metadata;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }
}
