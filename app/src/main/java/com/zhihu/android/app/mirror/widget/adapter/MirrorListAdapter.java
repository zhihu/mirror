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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.model.MirrorInfo;
import com.zhihu.android.app.mirror.util.DisplayUtils;
import com.zhihu.android.app.mirror.widget.holder.MirrorInfoHolder;
import com.zhihu.android.app.mirror.widget.holder.PlaceHolder;

public class MirrorListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_PLACE_HOLDER = 0x00;
    private static final int VIEW_TYPE_MIRROR_INFO_HOLDER = 0x01;

    private Context mContext;
    private List<MirrorInfo> mList;

    public MirrorListAdapter(Context context, List<MirrorInfo> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getItemCount() {
        return mList.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (0 < position && position <= mList.size()) {
            return VIEW_TYPE_MIRROR_INFO_HOLDER;
        } else {
            return VIEW_TYPE_PLACE_HOLDER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MIRROR_INFO_HOLDER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_mirror, parent, false);
            return new MirrorInfoHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_place, parent, false);
            return new PlaceHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) {
            ((PlaceHolder) holder).bind(DisplayUtils.getStatusBarHeight(mContext)
                    + mContext.getResources().getDimensionPixelSize(R.dimen.connect_status_height));
        } else if (0 < position && position <= mList.size()) {
            ((MirrorInfoHolder) holder).bind(mList.get(position - 1));
        } else if (mList.size() < position) {
            ((PlaceHolder) holder).bind(DisplayUtils.getNavigationBarHeight(mContext));
        }
    }
}
