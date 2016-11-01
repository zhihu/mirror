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
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import com.zhihu.android.app.mirror.util.DisplayUtils;

public class MirrorNameView extends AppCompatTextView {
    private static final float ALPHA_TO = 0.87F;
    private static final float SCALE_TO = 0.99F;
    private static final long DURATION = 100L;

    public MirrorNameView(Context context) {
        super(context);
    }

    public MirrorNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MirrorNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setMinWidth(DisplayUtils.getScreenWidth(getContext()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onActionRelease(event);
                break;
            default:
                break;
        }

        return true;
    }

    private void onActionDown(MotionEvent event) {
        setPivotX(event.getX());
        setPivotY(event.getY());
        animate().alpha(ALPHA_TO)
                .scaleX(SCALE_TO).scaleY(SCALE_TO)
                .setDuration(DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @SuppressWarnings("unused")
    private void onActionRelease(MotionEvent event) {
        animate().alpha(1.0F)
                .scaleX(1.0F).scaleY(1.0F)
                .setDuration(DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
