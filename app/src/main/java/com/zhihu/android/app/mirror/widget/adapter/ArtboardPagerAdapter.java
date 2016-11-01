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

package com.zhihu.android.app.mirror.widget.adapter;

import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.model.Artboard;
import com.zhihu.android.app.mirror.widget.ArtboardLayout;
import com.zhihu.android.app.mirror.widget.ArtboardView;

public class ArtboardPagerAdapter extends PagerAdapter {
    private List<Artboard> mList;

    private ArtboardLayout mCurrentLayout;
    private ArtboardView.ArtboardViewCallback mCallback;

    public ArtboardPagerAdapter(List<Artboard> list) {
        mList = list;
    }

    public void setArtboardViewCallback(ArtboardView.ArtboardViewCallback callback) {
        mCallback = callback;
    }

    @Nullable
    public ArtboardLayout getCurrentLayout() {
        return mCurrentLayout;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (!(object instanceof ArtboardLayout)) {
            return;
        }

        ArtboardLayout layout = (ArtboardLayout) object;
        if (layout != mCurrentLayout && mCurrentLayout != null) {
            mCurrentLayout.setArtboardViewCallback(null);
        }

        mCurrentLayout = layout;
        mCurrentLayout.setArtboardViewCallback(mCallback);
    }

    @Override
    public int getItemPosition(Object object) {
        if (mList.size() <= 0) {
            return POSITION_NONE;
        }

        View view = (View) object;
        int position = (int) view.getTag(R.id.artboard_position);
        String id = (String) view.getTag(R.id.artboard_id);

        int index = -1;
        for (int i = 0; i < mList.size(); i++) {
            Artboard artboard = mList.get(i);
            if (TextUtils.equals(artboard.getId(), id)) {
                index = i;
                break;
            }
        }

        if (index != position) {
            return POSITION_NONE;
        } else {
            Artboard artboard = mList.get(position);
            boolean needUpdate = artboard.isNeedUpdateInPager();
            artboard.setNeedUpdateInPager(false);
            return needUpdate ? POSITION_NONE : POSITION_UNCHANGED;
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ArtboardLayout layout = (ArtboardLayout) LayoutInflater.from(container.getContext())
                .inflate(R.layout.pager_item_artboard, container, false);
        container.addView(layout);

        Artboard artboard = mList.get(position);
        layout.setTag(R.id.artboard_position, position);
        layout.setTag(R.id.artboard_id, artboard.getId());
        layout.setArtboard(artboard);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
