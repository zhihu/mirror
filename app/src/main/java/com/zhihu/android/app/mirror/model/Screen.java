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

public class Screen {
    @SerializedName("name")
    private String mName;

    @SerializedName("scale")
    private float mScale;

    @SerializedName("width")
    private int mWidth;

    @SerializedName("height")
    private int mHeight;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        mScale = scale;
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
}
