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

import com.zhihu.android.app.mirror.model.Artboard;
import com.zhihu.android.app.mirror.widget.ArtboardLayout;

public class ArtboardHolder extends RecyclerView.ViewHolder {
    private ArtboardLayout mArtboardLayout;

    public ArtboardHolder(View view) {
        super(view);
        mArtboardLayout = (ArtboardLayout) view;
    }

    public void bind(Artboard artboard) {
        mArtboardLayout.setArtboard(artboard);
    }
}
