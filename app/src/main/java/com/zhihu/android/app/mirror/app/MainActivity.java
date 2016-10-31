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

package com.zhihu.android.app.mirror.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.LinkedList;
import java.util.List;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.event.ArtboardSelectedEvent;
import com.zhihu.android.app.mirror.event.ViewSwitchedEvent;
import com.zhihu.android.app.mirror.event.MirrorFoundEvent;
import com.zhihu.android.app.mirror.event.MirrorLostEvent;
import com.zhihu.android.app.mirror.event.MirrorMessageEvent;
import com.zhihu.android.app.mirror.event.MirrorResolveFailedEvent;
import com.zhihu.android.app.mirror.event.MirrorResolveSuccessEvent;
import com.zhihu.android.app.mirror.event.WebSocketCloseEvent;
import com.zhihu.android.app.mirror.event.WebSocketFailureEvent;
import com.zhihu.android.app.mirror.event.WifiConnectedEvent;
import com.zhihu.android.app.mirror.event.WifiConnectingEvent;
import com.zhihu.android.app.mirror.event.WifiDisconnectedEvent;
import com.zhihu.android.app.mirror.model.Artboard;
import com.zhihu.android.app.mirror.model.Content;
import com.zhihu.android.app.mirror.model.Message;
import com.zhihu.android.app.mirror.model.MirrorInfo;
import com.zhihu.android.app.mirror.util.AnimUtils;
import com.zhihu.android.app.mirror.util.DisplayUtils;
import com.zhihu.android.app.mirror.util.MirrorUtils;
import com.zhihu.android.app.mirror.util.NetworkUtils;
import com.zhihu.android.app.mirror.util.RxBus;
import com.zhihu.android.app.mirror.weiget.ArtboardLayout;
import com.zhihu.android.app.mirror.weiget.ArtboardPagerView;
import com.zhihu.android.app.mirror.weiget.ArtboardView;
import com.zhihu.android.app.mirror.weiget.ConnectStatusLayout;
import com.zhihu.android.app.mirror.weiget.adapter.ArtboardListAdapter;
import com.zhihu.android.app.mirror.weiget.adapter.ArtboardPagerAdapter;
import com.zhihu.android.app.mirror.weiget.adapter.MirrorListAdapter;
import okhttp3.OkHttpClient;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends RxAppCompatActivity implements ArtboardPagerView.ArtboardPagerViewDelegate,
        ArtboardView.ArtboardViewCallback {
    private static final int FLAG_MIRROR_LIST = ViewSwitchedEvent.FLAG_MIRROR_LIST;
    private static final int FLAG_ARTBOARD_LIST = ViewSwitchedEvent.FLAG_ARTBOARD_LIST;
    private static final int FLAG_ARTBOARD_PAGER = ViewSwitchedEvent.FLAG_ARTBOARD_PAGER;

    private FrameLayout mRootLayout;
    private LinearInterpolator mLinearInterpolator;
    private AccelerateInterpolator mAccelerateInterpolator;
    private String mCurrentManifestId;
    private int mCurrentFlag;

    private ConnectStatusLayout mConnectStatusLayout;
    private String mCurrentSSID;
    private MirrorInfo mCurrentMirror;

    private RecyclerView mMirrorListView;
    private MirrorListAdapter mMirrorListAdapter;
    private List<MirrorInfo> mMirrorInfoList;

    private RecyclerView mArtboardListView;
    private ArtboardListAdapter mArtboardListAdapter;
    private List<Artboard> mArtboardList;

    // share mArtboardList with mArtboardListView
    private ArtboardPagerView mArtboardPagerView;
    private ArtboardPagerAdapter mArtboardPagerAdapter;
    private boolean mIsDragDismiss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTaskDescription(new ActivityManager.TaskDescription(
                getString(R.string.app_name),
                BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                ContextCompat.getColor(this, R.color.black)));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mRootLayout = (FrameLayout) findViewById(R.id.root);
        mLinearInterpolator = new LinearInterpolator();
        mAccelerateInterpolator = new AccelerateInterpolator();
        mCurrentFlag = FLAG_MIRROR_LIST;

        setupFresco();
        setupMirrorListView();
        setupArtboardListView();
        setupArtboardPagerView();
        setupConnectStatusLayout();
        setupRxBus();

        Intent intent = new Intent(this, MirrorService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Fresco.shutDown();

        Intent intent = new Intent(this, MirrorService.class);
        stopService(intent);
    }

    // =============================================================================================

    private void setupFresco() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory.newBuilder(this, client)
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
    }

    private void setupMirrorListView() {
        mMirrorInfoList = new LinkedList<>();
        mMirrorListAdapter = new MirrorListAdapter(this, mMirrorInfoList);
        mMirrorListView = (RecyclerView) getLayoutInflater().inflate(R.layout.layout_artboard_list,
                mRootLayout, false);
        mMirrorListView.setAdapter(mMirrorListAdapter);
        mMirrorListView.setLayoutManager(new LinearLayoutManager(this));

        mMirrorListView.setVisibility(View.VISIBLE);
        mRootLayout.addView(mMirrorListView);
    }

    private void setupArtboardListView() {
        mArtboardList = new LinkedList<>();
        mArtboardListAdapter = new ArtboardListAdapter(this, mArtboardList);
        mArtboardListView = (RecyclerView) getLayoutInflater().inflate(R.layout.layout_artboard_list,
                mRootLayout, false);
        mArtboardListView.setAdapter(mArtboardListAdapter);

        final int gridSpanCount = getResources().getInteger(R.integer.grid_span_count);
        GridLayoutManager layoutManager = new GridLayoutManager(this, gridSpanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position <= 0 || position > mArtboardList.size() ? gridSpanCount : 1;
            }
        });
        mArtboardListView.setLayoutManager(layoutManager);

        mArtboardListView.setVisibility(View.INVISIBLE);
        mRootLayout.addView(mArtboardListView);
    }

    private void setupArtboardPagerView() {
        mArtboardPagerAdapter = new ArtboardPagerAdapter(mArtboardList);
        mArtboardPagerAdapter.setArtboardViewCallback(this);
        mArtboardPagerView = (ArtboardPagerView) getLayoutInflater().inflate(R.layout.layout_artboard_pager,
                mRootLayout, false);
        mArtboardPagerView.setAdapter(mArtboardPagerAdapter);
        mArtboardPagerView.setArtboardPagerViewDelegate(this);
        mArtboardPagerView.setOffscreenPageLimit(getResources().getInteger(R.integer.artboard_pager_limit));
        mArtboardPagerView.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));

        mArtboardPagerView.setVisibility(View.INVISIBLE);
        mRootLayout.addView(mArtboardPagerView);
    }

    @Override
    public boolean isArtboardPagerViewSwipeEnable() {
        ArtboardLayout layout = mArtboardPagerAdapter.getCurrentLayout();
        return layout != null && !layout.isScaling();
    }

    @Override
    public void onDrag(ArtboardView view, float dragDismissDistance, float dragTo) {
        dragTo = Math.abs(dragTo);
        if (dragTo > dragDismissDistance) {
            dragTo = dragDismissDistance;
        }

        int alpha = (int) (255.0F - 51.0F * dragTo / dragDismissDistance);
        mArtboardPagerView.getBackground().mutate().setAlpha(alpha);
    }

    @Override
    public void onDragDismiss(final ArtboardView view, boolean isDragDown) {
        mIsDragDismiss = true;
        switchToArtboardListView();
        view.animate().alpha(0.0F)
                .translationY(isDragDown ? -view.getHeight() : view.getHeight())
                .setDuration(AnimUtils.DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mIsDragDismiss = false;
                        mArtboardPagerView.getBackground().mutate().setAlpha(255);
                        view.setTranslationY(0.0F);
                        view.setAlpha(1.0F);
                    }
                })
                .start();
    }

    private void setupConnectStatusLayout() {
        mCurrentSSID = NetworkUtils.getCurrentSSID(this);
        mConnectStatusLayout = (ConnectStatusLayout) getLayoutInflater().inflate(R.layout.layout_connect_status,
                mRootLayout, false);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.CONNECTING, mCurrentSSID);

        mConnectStatusLayout.setVisibility(View.VISIBLE);
        mRootLayout.addView(mConnectStatusLayout);
    }

    // =============================================================================================

    @Override
    public void onBackPressed() {
        if (mCurrentFlag == FLAG_ARTBOARD_PAGER) {
            switchToArtboardListView();
        } else if (mCurrentFlag == FLAG_ARTBOARD_LIST) {
            switchToMirrorListView();
        } else {
            super.onBackPressed();
        }
    }

    private void switchToMirrorListView() {
        if (mCurrentFlag == FLAG_ARTBOARD_LIST) {
            AnimUtils.showViewAlphaAnim(mArtboardListView, mLinearInterpolator, false);
        } else if (mCurrentFlag == FLAG_ARTBOARD_PAGER) {
            AnimUtils.showViewAlphaAnim(mArtboardPagerView, mLinearInterpolator, false);
            AnimUtils.showViewAlphaAnim(mArtboardListView, mLinearInterpolator, false);
            AnimUtils.showViewAlphaAnim(mConnectStatusLayout, mLinearInterpolator, true);
            DisplayUtils.clearImmersiveStickyMode(this);
        }

        mArtboardList.clear();
        mArtboardPagerAdapter.notifyDataSetChanged();
        mArtboardListAdapter.notifyDataSetChanged();

        mCurrentFlag = FLAG_MIRROR_LIST;
        RxBus.getInstance().post(new ViewSwitchedEvent(FLAG_MIRROR_LIST));
    }

    private void switchToArtboardListView() {
        if (mCurrentFlag == FLAG_MIRROR_LIST) {
            AnimUtils.showViewAlphaAnim(mArtboardListView, mLinearInterpolator, true);
        } else if (mCurrentFlag == FLAG_ARTBOARD_PAGER) {
            TimeInterpolator interpolator;
            if (mIsDragDismiss) {
                interpolator = mAccelerateInterpolator;
            } else {
                interpolator = mLinearInterpolator;
            }
            AnimUtils.showViewAlphaAnim(mArtboardPagerView, interpolator, false);
            AnimUtils.showViewAlphaAnim(mConnectStatusLayout, interpolator, true);
            DisplayUtils.clearImmersiveStickyMode(this);
        }

        mCurrentFlag = FLAG_ARTBOARD_LIST;
        RxBus.getInstance().post(new ViewSwitchedEvent(FLAG_ARTBOARD_LIST));
    }

    private void switchToArtboardPagerView() {
        if (mCurrentFlag != FLAG_ARTBOARD_LIST) {
            return;
        }

        AnimUtils.showViewAlphaAnim(mConnectStatusLayout, mLinearInterpolator, false);
        AnimUtils.showViewAlphaAnim(mArtboardPagerView, mLinearInterpolator, true);
        DisplayUtils.requestImmersiveStickyMode(this);

        mCurrentFlag = FLAG_ARTBOARD_PAGER;
        RxBus.getInstance().post(new ViewSwitchedEvent(FLAG_ARTBOARD_PAGER));
    }

    // =============================================================================================

    // when failed, always back to MirrorListView;
    // and have any better code style?
    private void setupRxBus() {
        RxBus.getInstance().toObservable(WifiConnectingEvent.class)
                .onBackpressureBuffer()
                .compose(this.<WifiConnectingEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WifiConnectingEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(WifiConnectingEvent event) {
                        onWifiConnectingEvent();
                    }
                });

        RxBus.getInstance().toObservable(WifiConnectedEvent.class)
                .onBackpressureBuffer()
                .compose(this.<WifiConnectedEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WifiConnectedEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(WifiConnectedEvent event) {
                        onWifiConnectedEvent();
                    }
                });

        RxBus.getInstance().toObservable(WifiDisconnectedEvent.class)
                .onBackpressureBuffer()
                .compose(this.<WifiDisconnectedEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WifiDisconnectedEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(WifiDisconnectedEvent event) {
                        onWifiDisconnectedEvent();
                    }
                });

        RxBus.getInstance().toObservable(MirrorFoundEvent.class)
                .onBackpressureBuffer()
                .compose(this.<MirrorFoundEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MirrorFoundEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MirrorFoundEvent event) {
                        onMirrorFoundEvent(event);
                    }
                });

        RxBus.getInstance().toObservable(MirrorLostEvent.class)
                .onBackpressureBuffer()
                .compose(this.<MirrorLostEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MirrorLostEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MirrorLostEvent event) {
                        onMirrorLostEvent(event);
                    }
                });

        RxBus.getInstance().toObservable(MirrorResolveSuccessEvent.class)
                .onBackpressureBuffer()
                .compose(this.<MirrorResolveSuccessEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MirrorResolveSuccessEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MirrorResolveSuccessEvent event) {
                        onMirrorResolveSuccessEvent(event);
                    }
                });

        RxBus.getInstance().toObservable(MirrorResolveFailedEvent.class)
                .onBackpressureBuffer()
                .compose(this.<MirrorResolveFailedEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MirrorResolveFailedEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MirrorResolveFailedEvent event) {
                        onMirrorResolveFailedEvent(event);
                    }
                });

        RxBus.getInstance().toObservable(WebSocketCloseEvent.class)
                .onBackpressureBuffer()
                .compose(this.<WebSocketCloseEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WebSocketCloseEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(WebSocketCloseEvent event) {
                        onWebSocketCloseEvent(event);
                    }
                });

        RxBus.getInstance().toObservable(WebSocketFailureEvent.class)
                .onBackpressureBuffer()
                .compose(this.<WebSocketFailureEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WebSocketFailureEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(WebSocketFailureEvent event) {
                        onWebSocketFailureEvent(event);
                    }
                });

        RxBus.getInstance().toObservable(MirrorMessageEvent.class)
                .onBackpressureBuffer()
                .compose(this.<MirrorMessageEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MirrorMessageEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MirrorMessageEvent event) {
                        dispatchMirrorMessageEvent(event);
                    }
                });

        RxBus.getInstance().toObservable(ArtboardSelectedEvent.class)
                .onBackpressureBuffer()
                .compose(this.<ArtboardSelectedEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArtboardSelectedEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ArtboardSelectedEvent event) {
                        onArtboardSelectedEvent(event);
                    }
                });
    }

    private void onWifiConnectingEvent() {
        mCurrentSSID = NetworkUtils.getCurrentSSID(this);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.CONNECTING, mCurrentSSID);
    }

    private void onWifiConnectedEvent() {
        mCurrentSSID = NetworkUtils.getCurrentSSID(this);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.CONNECTED, mCurrentSSID);
    }

    private void onWifiDisconnectedEvent() {
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.DISCONNECTED, mCurrentSSID);
        switchToMirrorListView();
    }

    // just name equals, have a better way?
    private void onMirrorFoundEvent(MirrorFoundEvent event) {
        MirrorInfo mirrorInfo = event.getMirrorInfo();
        int position = -1;

        for (int i = 0 ; i < mMirrorInfoList.size(); i++) {
            MirrorInfo info = mMirrorInfoList.get(i);
            if (TextUtils.equals(info.getName(), mirrorInfo.getName())) {
                position = i;
                break;
            }
        }

        // add 1 for top placeholder
        if (position >= 0) {
            mMirrorInfoList.set(position, mirrorInfo);
            mMirrorListAdapter.notifyItemChanged(position + 1);
        } else {
            mMirrorInfoList.add(mirrorInfo);
            mMirrorListAdapter.notifyItemInserted(mMirrorInfoList.size());
        }
    }

    // just name equals, have a better way?
    private void onMirrorLostEvent(MirrorLostEvent event) {
        MirrorInfo mirrorInfo = event.getMirrorInfo();
        int position = -1;

        for (int i = 0 ; i < mMirrorInfoList.size(); i++) {
            MirrorInfo info = mMirrorInfoList.get(i);
            if (TextUtils.equals(info.getName(), mirrorInfo.getName())) {
                position = i;
                break;
            }
        }

        // add 1 for top placeholder
        if (position >= 0) {
            mMirrorInfoList.remove(position);
            mMirrorListAdapter.notifyItemRemoved(position + 1);
        }

        if (mCurrentMirror != null && TextUtils.equals(mCurrentMirror.getName(), mirrorInfo.getName())) {
            String currentSketch = MirrorUtils.getCurrentSketch(this, mCurrentMirror);
            mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.DISCONNECTED, currentSketch);
            switchToMirrorListView();
        }
    }

    private void onMirrorResolveSuccessEvent(MirrorResolveSuccessEvent event) {
        mCurrentMirror = event.getMirrorInfo();
        String currentSketch = MirrorUtils.getCurrentSketch(this, mCurrentMirror);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.CONNECTING, currentSketch);
    }

    private void onMirrorResolveFailedEvent(MirrorResolveFailedEvent event) {
        mCurrentMirror = event.getMirrorInfo();
        String currentSketch = MirrorUtils.getCurrentSketch(this, mCurrentMirror);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.DISCONNECTED, currentSketch);
        switchToMirrorListView();
    }

    private void onWebSocketCloseEvent(WebSocketCloseEvent event) {
        String currentSketch = MirrorUtils.getCurrentSketch(this, mCurrentMirror);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.DISCONNECTED, currentSketch);
        switchToMirrorListView();
    }

    private void onWebSocketFailureEvent(WebSocketFailureEvent event) {
        String currentSketch = MirrorUtils.getCurrentSketch(this, mCurrentMirror);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.DISCONNECTED, currentSketch);
        switchToMirrorListView();
    }

    private void dispatchMirrorMessageEvent(MirrorMessageEvent event) {
        Message message = event.getMessage();
        if (TextUtils.equals(message.getType(), MirrorUtils.TYPE_CONNECTED)) {
            onMirrorMessageConnected();
        } else if (TextUtils.equals(message.getType(), MirrorUtils.TYPE_DISCONNECTED)) {
            onMirrorMessageDisconnected();
        } else if (TextUtils.equals(message.getType(), MirrorUtils.TYPE_MANIFEST)) {
            onMirrorMessageManifest(message);
        } else if (TextUtils.equals(message.getType(), MirrorUtils.TYPE_ARTBOARD)) {
            onMirrorMessageArtboard(message);
        } else if (TextUtils.equals(message.getType(), MirrorUtils.TYPE_CURRENT_ARTBOARD)) {
            onMirrorMessageCurrentArtboard(message);
        }
    }

    private void onMirrorMessageConnected() {
        String currentSketch = MirrorUtils.getCurrentSketch(this, mCurrentMirror);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.CONNECTED, currentSketch);
        switchToArtboardListView();
    }

    private void onMirrorMessageDisconnected() {
        String currentSketch = MirrorUtils.getCurrentSketch(this, mCurrentMirror);
        mConnectStatusLayout.setConnectStatus(ConnectStatusLayout.DISCONNECTED, currentSketch);
        switchToMirrorListView();
    }

    private void onMirrorMessageManifest(Message message) {
        Content content = message.getContent();

        // when id difference, means Sketch file changed
        if (TextUtils.isEmpty(mCurrentManifestId)) {
            mCurrentManifestId = content.getId();
        } else {
            if (!TextUtils.equals(content.getId(), mCurrentManifestId)) {
                mCurrentManifestId = content.getId();
                switchToArtboardListView();
            }
        }

        final List<Artboard> oldList = new LinkedList<>(mArtboardList);
        mArtboardList.clear();
        mArtboardList.addAll(MirrorUtils.getArtboardsFromContent(this, content));
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return mArtboardList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Artboard old = oldList.get(oldItemPosition);
                Artboard nez = mArtboardList.get(newItemPosition);
                return TextUtils.equals(old.getId(), nez.getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Artboard old = oldList.get(oldItemPosition);
                Artboard nez = mArtboardList.get(newItemPosition);
                nez.setNeedUpdateInPager(old.isNeedUpdateInPager()); // when pager updated then false
                return !old.isNeedUpdateInList() && old.equals(nez);
            }
        });

        // add 1 for top placeholder
        result.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                mArtboardListAdapter.notifyItemRangeInserted(position + 1, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                mArtboardListAdapter.notifyItemRangeRemoved(position + 1, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                mArtboardListAdapter.notifyItemMoved(fromPosition + 1, toPosition + 1);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                mArtboardListAdapter.notifyItemRangeChanged(position + 1, count, payload);
            }
        });

        mArtboardPagerAdapter.notifyDataSetChanged();
    }

    // also update when next TYPE_MANIFEST arrive
    private void onMirrorMessageArtboard(Message message) {
        Content content = message.getContent();
        int position = -1;

        for (int i = 0; i < mArtboardList.size(); i++) {
            Artboard artboard = mArtboardList.get(i);
            if (TextUtils.equals(artboard.getId(), content.getIdentifier())) {
                artboard.setNeedUpdateInList(true);
                artboard.setNeedUpdateInPager(true);
                position = i;
                break;
            }
        }

        // add 1 for top placeholder
        if (position >= 0) {
            mArtboardListAdapter.notifyItemChanged(position + 1);
            mArtboardPagerAdapter.notifyDataSetChanged();
            mArtboardPagerView.setCurrentItem(position, false);
        }
    }

    // not much use
    private void onMirrorMessageCurrentArtboard(Message message) {
        String id = message.getContent().getIdentifier();
        int position = -1;

        for (int i = 0; i < mArtboardList.size(); i++) {
            Artboard artboard = mArtboardList.get(i);
            if (TextUtils.equals(artboard.getId(), id)) {
                position = i;
                break;
            }
        }

        if (position >= 0) {
            mArtboardPagerView.setCurrentItem(position, false);
        }
    }

    private void onArtboardSelectedEvent(ArtboardSelectedEvent event) {
        Artboard artboard = event.getArtboard();
        int position = mArtboardList.indexOf(artboard);
        if (position < 0) {
            return;
        }

        mArtboardPagerView.setCurrentItem(position, false);
        switchToArtboardPagerView();
    }
}
