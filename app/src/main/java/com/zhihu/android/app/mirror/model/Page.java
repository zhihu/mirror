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

public class Page {
    @SerializedName("id")
    private String mId;

    @SerializedName("slug")
    private String mSlug;

    @SerializedName("name")
    private String mName;

    @SerializedName("artboards")
    private Artboard[] mArtboards;

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

    public Artboard[] getArtboards() {
        return mArtboards;
    }

    public void setArtboards(Artboard[] artboards) {
        mArtboards = artboards;
    }
}
