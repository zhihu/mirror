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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.util.DisplayUtils;

public class PointView extends View {
    private Paint mPaint;
    private int mAlpha;
    private int mRadius;

    private float mCurrentAlpha;
    private float mDeltaFraction;
    private long mPreDrawTime;
    private boolean mIsRunning;
    private boolean mIsReverse;

    public PointView(Context context) {
        super(context);
        init(null);
    }

    public PointView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mAlpha = 255;
        mRadius = DisplayUtils.dp2px(getContext(), 8.0F);
        int color = Color.WHITE;
        int duration = 255;
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.PointView);
            mAlpha = array.getInt(R.styleable.PointView_pvAlpha, mAlpha);
            mRadius = array.getDimensionPixelSize(R.styleable.PointView_pvRadius, mRadius);
            color = array.getColor(R.styleable.PointView_pvColor, color);
            duration = array.getInt(R.styleable.PointView_pvDuration, duration);
            array.recycle();
        }

        mCurrentAlpha = 255.0F;
        mDeltaFraction = (mCurrentAlpha - mAlpha) / duration;
        mIsRunning = false;
        mIsReverse = false;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha((int) mCurrentAlpha);
        mPaint.setColor(color);
    }

    public void startBreath() {
        mIsRunning = true;
        mPreDrawTime = System.currentTimeMillis();
        invalidate();
    }

    public void stopBreath() {
        mIsRunning = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mIsRunning) {
            mCurrentAlpha = 255.0F;
            mPaint.setAlpha((int) mCurrentAlpha);
            canvas.drawCircle(getPivotX(), getPivotY(), mRadius, mPaint);
            return;
        }

        float delta = (System.currentTimeMillis() - mPreDrawTime) * mDeltaFraction;
        if (!mIsReverse && mCurrentAlpha - delta < mAlpha) {
            mCurrentAlpha = mAlpha;
            mIsReverse = true;
        } else if (mIsReverse && mCurrentAlpha + delta > 255.0F) {
            mCurrentAlpha = 255.0F;
            mIsReverse = false;
        } else {
            mCurrentAlpha = !mIsReverse ? mCurrentAlpha - delta : mCurrentAlpha + delta;
        }

        mPaint.setAlpha((int) mCurrentAlpha);
        canvas.drawCircle(getPivotX(), getPivotY(), mRadius, mPaint);
        mPreDrawTime = System.currentTimeMillis();
        invalidate();
    }
}
