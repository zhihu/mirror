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

package com.zhihu.android.app.mirror.widget.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class PlaceHolder extends RecyclerView.ViewHolder {
    private View mView;

    public PlaceHolder(View view) {
        super(view);
        mView = view;
    }

    public void bind(int height) {
        mView.getLayoutParams().height = height;
        mView.requestLayout();
    }
}
