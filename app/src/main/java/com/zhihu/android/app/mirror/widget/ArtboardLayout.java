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
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.model.Artboard;
import com.zhihu.android.app.mirror.util.AnimUtils;
import com.zhihu.android.app.mirror.util.MirrorUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ArtboardLayout extends FrameLayout implements SubsamplingScaleImageView.OnImageEventListener {
    private ArtboardView mArtboardView;
    private ProgressBar mProgressBar;

    private Subscription mSubscription;
    private LinearInterpolator mInterpolator;

    public ArtboardLayout(Context context) {
        super(context);
    }

    public ArtboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArtboardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setArtboardViewCallback(ArtboardView.ArtboardViewCallback callback) {
        mArtboardView.setArtboardViewCallback(callback);
    }

    public void setArtboard(Artboard artboard) {
        AnimUtils.showViewAlphaAnim(mProgressBar, mInterpolator, true);

        String url = MirrorUtils.buildArtboardHttpUrl(getContext(), artboard);
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).build();
        DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline()
                .fetchDecodedImage(request, getContext(), ImageRequest.RequestLevel.FULL_FETCH);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(final Bitmap bitmap) {
                if (bitmap == null || bitmap.isRecycled()) {
                    return;
                }

                if (mSubscription != null) {
                    mSubscription.unsubscribe();
                }

                mSubscription = Observable.create(
                        new Observable.OnSubscribe<Bitmap>() {
                            @Override
                            public void call(Subscriber<? super Bitmap> subscriber) {
                                Bitmap copy = bitmap.copy(bitmap.getConfig(), true);
                                subscriber.onNext(copy);
                                subscriber.onCompleted();
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Bitmap>() {
                            @Override
                            public void onCompleted() {
                                // DO NOTHING
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Bitmap bitmap) {
                                mArtboardView.setImage(ImageSource.bitmap(bitmap));
                            }
                        });
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                AnimUtils.showViewAlphaAnim(mProgressBar, mInterpolator, true);
            }
        }, UiThreadImmediateExecutorService.getInstance());
    }

    public boolean isScaling() {
        return mArtboardView.isScaling();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mArtboardView = (ArtboardView) findViewById(R.id.artboard);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        mArtboardView.setOnImageEventListener(this);
        mInterpolator = new LinearInterpolator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mArtboardView.setArtboardViewCallback(null);
        mArtboardView.setOnImageEventListener(null);
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onReady() {
        // DO NOTHING
    }

    @Override
    public void onPreviewLoadError(Exception e) {
        // DO NOTHING
    }

    @Override
    public void onTileLoadError(Exception e) {
        // DO NOTHING
    }

    @Override
    public void onImageLoadError(Exception e) {
        AnimUtils.showViewAlphaAnim(mProgressBar, mInterpolator, true);
    }

    @Override
    public void onImageLoaded() {
        AnimUtils.showViewAlphaAnim(mProgressBar, mInterpolator, false);
    }
}
