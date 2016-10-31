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

public class Handshake {
    @SerializedName("type")
    private String mType;

    @SerializedName("instance-id")
    private String mInstanceId;

    @SerializedName("content")
    private Content mContent;

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getInstanceId() {
        return mInstanceId;
    }

    public void setInstanceId(String instanceId) {
        mInstanceId = instanceId;
    }

    public Content getContent() {
        return mContent;
    }

    public void setContent(Content content) {
        mContent = content;
    }
}
