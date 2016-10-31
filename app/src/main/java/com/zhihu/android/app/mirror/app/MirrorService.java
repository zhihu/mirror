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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.zhihu.android.app.mirror.event.MirrorFoundEvent;
import com.zhihu.android.app.mirror.event.MirrorLostEvent;
import com.zhihu.android.app.mirror.event.MirrorMessageEvent;
import com.zhihu.android.app.mirror.event.MirrorResolveFailedEvent;
import com.zhihu.android.app.mirror.event.MirrorResolveSuccessEvent;
import com.zhihu.android.app.mirror.event.MirrorSelectedEvent;
import com.zhihu.android.app.mirror.event.ViewSwitchedEvent;
import com.zhihu.android.app.mirror.event.WebSocketCloseEvent;
import com.zhihu.android.app.mirror.event.WebSocketFailureEvent;
import com.zhihu.android.app.mirror.event.WifiConnectedEvent;
import com.zhihu.android.app.mirror.event.WifiConnectingEvent;
import com.zhihu.android.app.mirror.event.WifiDisconnectedEvent;
import com.zhihu.android.app.mirror.model.Message;
import com.zhihu.android.app.mirror.model.MirrorInfo;
import com.zhihu.android.app.mirror.util.NetworkUtils;
import com.zhihu.android.app.mirror.util.MirrorUtils;
import com.zhihu.android.app.mirror.util.PreferenceUtils;
import com.zhihu.android.app.mirror.util.RxBus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class MirrorService extends Service implements WebSocketListener {
    private static final String SERVICE_TYPE = MirrorUtils.MIRROR_TYPE;
    private static final int PROTOCOL_TYPE = NsdManager.PROTOCOL_DNS_SD;

    private BroadcastReceiver mWifiReceiver;
    private boolean mIsWifiConnected;
    private boolean mIsWifiDisconnected;

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;

    private WebSocketCall mWebSocketCall;
    private Gson mGson;

    private CompositeSubscription mCompositeSubscription;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupWifiConnect();
        setupRxBus();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiverSafety();
        closeDiscoveryListenerSafety();
        closeWebSocketSafety();

        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
    }

    private void unregisterReceiverSafety() {
        try {
            if (mWifiReceiver != null) {
                unregisterReceiver(mWifiReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeDiscoveryListenerSafety() {
        try {
            if (mNsdManager != null && mDiscoveryListener != null) {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeWebSocketSafety() {
        try {
            if (mWebSocketCall != null) {
                mWebSocketCall.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================================================================

    private void setupWifiConnect() {
        unregisterReceiverSafety();
        closeDiscoveryListenerSafety();
        closeWebSocketSafety();

        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!TextUtils.equals(ConnectivityManager.CONNECTIVITY_ACTION, intent.getAction())) {
                    return;
                }

                ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo info = manager.getActiveNetworkInfo();
                if (info == null) {
                    onWifiDisconnected();
                    return;
                }

                if (info.getType() != ConnectivityManager.TYPE_WIFI) {
                    onWifiDisconnected();
                    return;
                }

                if (!info.isConnected()) {
                    onWifiConnecting();
                } else {
                    onWifiConnected();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, filter);

        if (!NetworkUtils.isWifiConnected(this)) {
            NetworkUtils.setWifiEnabled(this, true);
        }
    }

    private void onWifiConnecting() {
        RxBus.getInstance().post(new WifiConnectingEvent());
    }

    private void onWifiConnected() {
        if (mIsWifiConnected) {
            return;
        }
        mIsWifiConnected = true;
        mIsWifiDisconnected = false;

        closeDiscoveryListenerSafety();
        RxBus.getInstance().post(new WifiConnectedEvent());

        mNsdManager = (NsdManager) getSystemService(NSD_SERVICE);
        mDiscoveryListener = buildDiscoveryListener();
        mNsdManager.discoverServices(SERVICE_TYPE, PROTOCOL_TYPE, mDiscoveryListener);
    }

    private NsdManager.DiscoveryListener buildDiscoveryListener() {
        return new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                // DO NOTHING
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                // DO NOTHING
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                // DO NOTHING
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                // DO NOTHING
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                if (nsdServiceInfo.getServiceName().startsWith(MirrorUtils.MIRROR_FILTER)) {
                    RxBus.getInstance().post(new MirrorFoundEvent(new MirrorInfo(nsdServiceInfo)));
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                if (nsdServiceInfo.getServiceName().startsWith(MirrorUtils.MIRROR_FILTER)) {
                    RxBus.getInstance().post(new MirrorLostEvent(new MirrorInfo(nsdServiceInfo)));
                }
            }
        };
    }

    private void onWifiDisconnected() {
        if (mIsWifiDisconnected) {
            return;
        }
        mIsWifiConnected = false;
        mIsWifiDisconnected = true;

        closeDiscoveryListenerSafety();
        closeWebSocketSafety();
        RxBus.getInstance().post(new WifiDisconnectedEvent());
    }

    // =============================================================================================

    private void setupRxBus() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
        mCompositeSubscription = new CompositeSubscription();

        Subscription subscription = RxBus.getInstance().toObservable(MirrorSelectedEvent.class)
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MirrorSelectedEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MirrorSelectedEvent event) {
                        onMirrorSelectedEvent(event);
                    }
                });
        mCompositeSubscription.add(subscription);

        subscription = RxBus.getInstance().toObservable(ViewSwitchedEvent.class)
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ViewSwitchedEvent>() {
                    @Override
                    public void onCompleted() {
                        // DO NOTHING
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ViewSwitchedEvent event) {
                        onViewSwitchedEvent(event);
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    private void onMirrorSelectedEvent(MirrorSelectedEvent event) {
        closeWebSocketSafety();
        mNsdManager.resolveService(event.getMirrorInfo().getNsdServiceInfo(),
                buildResolveListener());
    }

    private NsdManager.ResolveListener buildResolveListener() {
        return new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int errorCode) {
                MirrorService.this.onResolveFailed(nsdServiceInfo);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                onResolveSuccess(nsdServiceInfo);
            }
        };
    }

    private void onResolveFailed(NsdServiceInfo nsdServiceInfo) {
        RxBus.getInstance().post(new MirrorResolveFailedEvent(new MirrorInfo(nsdServiceInfo)));
    }

    private void onResolveSuccess(NsdServiceInfo nsdServiceInfo) {
        MirrorInfo mirrorInfo = new MirrorInfo(nsdServiceInfo);
        PreferenceUtils.setMirror(this, MirrorUtils.buildMirrorHttpUrl(mirrorInfo));
        RxBus.getInstance().post(new MirrorResolveSuccessEvent(mirrorInfo));

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(MirrorUtils.MIRROR_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(MirrorUtils.MIRROR_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
        Request request = new Request.Builder()
                .url(MirrorUtils.buildMirrorWebSocketUrl(mirrorInfo))
                .build();
        mWebSocketCall = WebSocketCall.create(client, request);
        mWebSocketCall.enqueue(this);
    }

    private void onViewSwitchedEvent(ViewSwitchedEvent event) {
        if (event.getCurrentType() == ViewSwitchedEvent.FLAG_MIRROR_LIST) {
            closeWebSocketSafety();
        }
    }

    // =============================================================================================

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        mGson = new Gson();
        String json = mGson.toJson(MirrorUtils.buildHandshake(this));

        try {
            webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, json));
        } catch (IOException e) {
            onFailure(e, null);
        }
    }

    @Override
    public void onFailure(IOException e, Response response) {
        e.printStackTrace(); // sometimes EOF, how to fix?
        RxBus.getInstance().post(new WebSocketFailureEvent(e, response));
    }

    @Override
    public void onMessage(ResponseBody body) throws IOException {
        Message message = mGson.fromJson(body.string(), Message.class);

        // not TYPE_DISCONNECTED
        if (TextUtils.equals(MirrorUtils.TYPE_CONNECTED, message.getType())) {
            PreferenceUtils.setToken(this, message.getContent().getToken());
            PreferenceUtils.setDevice(this, message.getContent().getDevice());
        }

        RxBus.getInstance().post(new MirrorMessageEvent(message));
        body.close();
    }

    @Override
    public void onPong(Buffer payload) {
        // DO NOTHING
    }

    @Override
    public void onClose(int code, String reason) {
        RxBus.getInstance().post(new WebSocketCloseEvent(code, reason));
    }
}
