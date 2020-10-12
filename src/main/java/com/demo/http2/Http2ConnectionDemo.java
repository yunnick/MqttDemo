package com.demo.http2;

import okhttp3.internal.http2.Http2;
import okhttp3.internal.http2.Http2Connection;
import okhttp3.internal.http2.PushObserver;

import java.io.IOException;
import java.net.Socket;

import static okhttp3.internal.http2.Http2Connection.Listener.REFUSE_INCOMING_STREAMS;

public class Http2ConnectionDemo {
    public static void main(String[] args) throws IOException {

        Http2Connection connection = new Http2Connection.Builder(true)
                .socket(new Socket())
                .pushObserver(PushObserver.CANCEL)
                .listener(REFUSE_INCOMING_STREAMS)
                .build();
        connection.start();



    }
}
