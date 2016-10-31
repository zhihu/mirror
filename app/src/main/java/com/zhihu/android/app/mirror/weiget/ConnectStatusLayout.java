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

package com.zhihu.android.app.mirror.weiget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zhihu.android.app.mirror.R;
import com.zhihu.android.app.mirror.util.DisplayUtils;

public class ConnectStatusLayout extends LinearLayout {
    public static final int CONNECTING = 0x00;
    public static final int CONNECTED = 0x01;
    public static final int DISCONNECTED = 0x02;

    @IntDef({CONNECTING, CONNECTED, DISCONNECTED})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface ConnectStatus {}

    private PointView mPointView;
    private AppCompatTextView mMessageView;

    public ConnectStatusLayout(Context context) {
        super(context);
    }

    public ConnectStatusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConnectStatusLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setConnectStatus(@ConnectStatus int status, String tmpl) {
        switch (status) {
            case CONNECTED:
                mPointView.stopBreath();
                mMessageView.setText(getResources().getString(R.string.connect_status_connected, tmpl));
                break;
            case DISCONNECTED:
                mPointView.startBreath();
                mMessageView.setText(getResources().getString(R.string.connect_status_disconnected, tmpl));
                break;
            case CONNECTING:
            default:
                mPointView.startBreath();
                mMessageView.setText(getResources().getString(R.string.connect_status_connecting, tmpl));
                break;
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        ((FrameLayout.LayoutParams) getLayoutParams()).topMargin = DisplayUtils.getStatusBarHeight(getContext());
        requestLayout();

        mPointView = (PointView) findViewById(R.id.point);
        mMessageView = (AppCompatTextView) findViewById(R.id.message);
    }
}
