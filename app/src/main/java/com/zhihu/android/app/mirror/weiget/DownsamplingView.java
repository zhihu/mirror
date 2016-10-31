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

package com.zhihu.android.app.mirror.weiget;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.util.DisplayUtils;

public class DownsamplingView extends SimpleDraweeView {
    private static final float W_H_RATIO = 1.0F;
    private static final int FADE_DURATION = 300;

    private static final float ALPHA_TO = 0.87F;
    private static final float SCALE_TO = 0.99F;
    private static final long DURATION = 100L;

    private int mDownsamplingViewWidth;
    private int mDownsamplingViewHeight;

    public DownsamplingView(Context context) {
        super(context);
    }

    public DownsamplingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DownsamplingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getDownsamplingViewWidth() {
        return mDownsamplingViewWidth;
    }

    public int getDownsamplingViewHeight() {
        return mDownsamplingViewHeight;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int gridSpanCount = getResources().getInteger(R.integer.grid_span_count);
        float width = DisplayUtils.getScreenWidth(getContext()) / gridSpanCount;
        float height = width / W_H_RATIO;
        mDownsamplingViewWidth = (int) width;
        mDownsamplingViewHeight = (int) height;

        getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FOCUS_CROP);
        getHierarchy().setActualImageFocusPoint(new PointF(0.5F, 0.0F)); // top center
        getHierarchy().setFadeDuration(FADE_DURATION);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mDownsamplingViewWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mDownsamplingViewHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
