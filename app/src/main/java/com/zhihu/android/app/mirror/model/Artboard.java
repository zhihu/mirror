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

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class Artboard {
    @SerializedName("id")
    private String mId;

    @SerializedName("slug")
    private String mSlug;

    // @SerializedName("files")
    // private File[] mFiles;

    @SerializedName("name")
    private String mName;

    @SerializedName("width")
    private int mWidth;

    @SerializedName("height")
    private int mHeight;

    @SerializedName("path")
    private String mPath;

    // force to update
    private boolean mNeedUpdateInList;

    private boolean mNeedUpdateInPager;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getSlug() {
        return mSlug;
    }

    public void setSlug(String slug) {
        mSlug = slug;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public boolean isNeedUpdateInList() {
        return mNeedUpdateInList;
    }

    public void setNeedUpdateInList(boolean needUpdateInList) {
        mNeedUpdateInList = needUpdateInList;
    }

    public boolean isNeedUpdateInPager() {
        return mNeedUpdateInPager;
    }

    public void setNeedUpdateInPager(boolean needUpdateInPager) {
        mNeedUpdateInPager = needUpdateInPager;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Artboard)) {
            return false;
        }

        if (this == object) {
            return true;
        }

        Artboard artboard = (Artboard) object;
        return TextUtils.equals(mId, artboard.mId)
                && TextUtils.equals(mSlug, artboard.getSlug())
                && TextUtils.equals(mName, artboard.getName())
                && mWidth == artboard.getWidth()
                && mHeight == artboard.getHeight();
    }
}
