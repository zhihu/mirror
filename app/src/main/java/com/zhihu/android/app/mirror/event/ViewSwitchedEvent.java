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

package com.zhihu.android.app.mirror.event;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ViewSwitchedEvent {
    public static final int FLAG_MIRROR_LIST = 0x00;
    public static final int FLAG_ARTBOARD_LIST = 0x01;
    public static final int FLAG_ARTBOARD_PAGER = 0x02;

    @IntDef({FLAG_MIRROR_LIST, FLAG_ARTBOARD_LIST, FLAG_ARTBOARD_PAGER})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface BackPressedType {}

    @BackPressedType
    private int mCurrentType;

    public ViewSwitchedEvent(@BackPressedType int currentType) {
        mCurrentType = currentType;
    }

    public int getCurrentType() {
        return mCurrentType;
    }

    public void setCurrentType(int currentType) {
        mCurrentType = currentType;
    }
}
