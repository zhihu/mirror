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

package com.zhihu.android.app.mirror.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.view.View;

public class AnimUtils {
    public static final int DURATION = 300; // ms

    public static void showViewAlphaAnim(final View view, TimeInterpolator interpolator, final boolean visible) {
        if (visible && view.getVisibility() == View.VISIBLE
                || !visible && view.getVisibility() != View.VISIBLE) {
            return;
        }

        float fromAlpha = visible ? 0.0F : 1.0F;
        float toAlpha = visible ? 1.0F : 0.0F;
        view.setAlpha(fromAlpha);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(toAlpha)
                .setDuration(DURATION)
                .setInterpolator(interpolator)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (visible) {
                            view.setAlpha(1.0F);
                            view.setVisibility(View.VISIBLE);
                        } else {
                            view.setAlpha(0.0F);
                            view.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .start();
    }
}
