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

package com.zhihu.android.app.mirror.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ArtboardPagerView extends ViewPager {
    public interface ArtboardPagerViewDelegate {
        boolean isArtboardPagerViewSwipeEnable();
    }

    private ArtboardPagerViewDelegate mDelegate;

    public ArtboardPagerView(Context context) {
        super(context);
    }

    public ArtboardPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setArtboardPagerViewDelegate(ArtboardPagerViewDelegate delegate) {
        mDelegate = delegate;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mDelegate != null) {
            return mDelegate.isArtboardPagerViewSwipeEnable()
                    && super.onInterceptTouchEvent(event);
        }

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDelegate != null) {
            return !mDelegate.isArtboardPagerViewSwipeEnable()
                    || super.onTouchEvent(event);
        }

        return super.onTouchEvent(event);
    }
}
