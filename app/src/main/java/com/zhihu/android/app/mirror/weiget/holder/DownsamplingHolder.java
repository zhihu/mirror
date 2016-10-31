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

package com.zhihu.android.app.mirror.weiget.holder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.event.ArtboardSelectedEvent;
import com.zhihu.android.app.mirror.model.Artboard;
import com.zhihu.android.app.mirror.util.MirrorUtils;
import com.zhihu.android.app.mirror.util.RxBus;
import com.zhihu.android.app.mirror.weiget.DownsamplingView;

public class DownsamplingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private DownsamplingView mDownsamplingView;
    private Artboard mArtboard;

    public DownsamplingHolder(View view) {
        super(view);

        mDownsamplingView = (DownsamplingView) view.findViewById(R.id.downsampling);
        mDownsamplingView.setOnClickListener(this);
    }

    public void bind(Artboard artboard) {
        mArtboard = artboard;

        String url = MirrorUtils.buildArtboardHttpUrl(mDownsamplingView.getContext(), mArtboard);
        int mResizeWidth = mDownsamplingView.getDownsamplingViewWidth();
        int mResizeHeight = mDownsamplingView.getDownsamplingViewHeight();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setResizeOptions(new ResizeOptions(mResizeWidth, mResizeHeight))
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(mDownsamplingView.getController())
                .setImageRequest(request)
                .build();
        mDownsamplingView.setController(controller);
    }

    @Override
    public void onClick(View view) {
        if (view == mDownsamplingView) {
            RxBus.getInstance().post(new ArtboardSelectedEvent(mArtboard));
        }
    }
}
