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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.math.BigDecimal;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.util.AnimUtils;
import com.zhihu.android.app.mirror.util.DisplayUtils;

// https://github.com/nickbutcher/plaid/blob/master/app/src/main/java/io/plaidapp/ui/widget/ElasticDragDismissFrameLayout.java
public class ArtboardView extends SubsamplingScaleImageView implements View.OnTouchListener {
    public interface ArtboardViewCallback {
        void onDrag(ArtboardView view, float dragDismissDistance, float dragTo);
        void onDragDismiss(ArtboardView view, boolean isDragDown);
    }

    private float mDragDismissDistance;
    private float mDragElacticity;

    private ObjectAnimator mTransYAnim;
    private float mActionDownY;
    private float mDragDistance;
    private boolean mIsDragDown; // drag direction to screen bottom

    private ArtboardViewCallback mCallback;
    private GestureDetector mGestureDetector;

    public ArtboardView(Context context) {
        super(context);
        init(null);
    }

    public ArtboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mDragDismissDistance = DisplayUtils.dp2px(getContext(), 112.0F);
        mDragElacticity = 0.8F;
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ArtboardView);
            mDragDismissDistance = array.getDimensionPixelOffset(R.styleable.ArtboardView_avDragDismissDistance,
                    (int) mDragDismissDistance);
            mDragElacticity = array.getFloat(R.styleable.ArtboardView_avDragElasticity, mDragElacticity);
            array.recycle();
        }

        mGestureDetector = new GestureDetector(getContext(), buildSimpleOnGestureListener());
        setOnTouchListener(this);
    }

    // always scale to the screen width side
    private GestureDetector.SimpleOnGestureListener buildSimpleOnGestureListener() {
        return new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                if (!isReady()) {
                    return true;
                }

                float scale;
                float screenWidth = DisplayUtils.getScreenWidth(getContext());
                if (getSWidth() >= getSHeight()) {
                    if (getSWidth() >= screenWidth) {
                        scale = getMinScale();
                    } else {
                        scale = screenWidth / (float) getSWidth();
                    }
                } else {
                    scale = screenWidth / (float) getSWidth();
                }

                animateScaleAndCenter(!isScaling() ? scale : getMinScale(), new PointF(event.getX(), event.getY()))
                        .withDuration(AnimUtils.DURATION)
                        .withEasing(SubsamplingScaleImageView.EASE_OUT_QUAD)
                        .withInterruptible(false)
                        .start();
                return true;
            }
        };
    }

    public void setArtboardViewCallback(ArtboardViewCallback callback) {
        mCallback = callback;
    }

    // ensure getScale() == getMinScale()
    public boolean isScaling() {
        float scale;
        float minScale;
        try {
            BigDecimal decimal = new BigDecimal(getScale());
            scale = decimal.setScale(2, BigDecimal.ROUND_FLOOR).floatValue();
            decimal = new BigDecimal(getMinScale());
            minScale = decimal.setScale(2, BigDecimal.ROUND_FLOOR).floatValue();
            return scale > minScale;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCallback = null;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        if (isImageLoaded() && !isScaling()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mTransYAnim != null && mTransYAnim.isRunning()) {
                        mTransYAnim.cancel();
                    }
                    mActionDownY = event.getY();
                    mDragDistance = 0.0F;
                    mIsDragDown = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    onActionMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    onActionRelease();
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    private void onActionMove(MotionEvent event) {
        mDragDistance = event.getY() - mActionDownY;
        mIsDragDown = mDragDistance > 0.0F;

        float dragFraction = (float) Math.log10(1.0F + (Math.abs(mDragDistance) / mDragDismissDistance));
        float dragTo = dragFraction * mDragDismissDistance * mDragElacticity;
        setTranslationY(mIsDragDown ? dragTo : -dragTo);
        if (mCallback != null) {
            mCallback.onDrag(this, mDragDismissDistance, dragTo);
        }
    }

    private void onActionRelease() {
        if (mCallback != null && Math.abs(mDragDistance) > mDragDismissDistance) {
            mCallback.onDragDismiss(this, mIsDragDown);
        } else {
            if (mTransYAnim != null && mTransYAnim.isRunning()) {
                mTransYAnim.cancel();
            }

            mTransYAnim = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, getTranslationY(), 0.0F);
            mTransYAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float dragTo = (Float) animation.getAnimatedValue();
                    if (mCallback != null) {
                        mCallback.onDrag(ArtboardView.this, mDragDismissDistance, dragTo);
                    }
                }
            });
            mTransYAnim.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            mTransYAnim.setInterpolator(PathInterpolatorCompat.create(0.4F, 0.0F, 0.2F, 1.0F));
            mTransYAnim.start();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return mGestureDetector.onTouchEvent(motionEvent);
    }
}
