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

package com.zhihu.android.app.mirror.event;

import java.io.IOException;

import okhttp3.Response;

public class WebSocketFailureEvent {
    private IOException mIOException;
    private Response mResponse;

    public WebSocketFailureEvent(IOException ioException, Response response) {
        mIOException = ioException;
        mResponse = response;
    }

    public IOException getIOException() {
        return mIOException;
    }

    public void setIOException(IOException IOException) {
        mIOException = IOException;
    }

    public Response getResponse() {
        return mResponse;
    }

    public void setResponse(Response response) {
        mResponse = response;
    }
}
