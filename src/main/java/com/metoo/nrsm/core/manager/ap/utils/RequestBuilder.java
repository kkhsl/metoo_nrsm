package com.metoo.nrsm.core.manager.ap.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 14:50
 */
public class RequestBuilder {

    private static final AtomicInteger nextId = new AtomicInteger(1);
    private RequestParams request = new RequestParams();

    private RequestBuilder() {
    }

    public static RequestBuilder newBuilder() {
        return new RequestBuilder();
    }

    public RequestParams build() {
        return this.request;
    }

    public RequestBuilder paramEntry(String key, Object value) {
        this.request.putParam(key, value);
        return this;
    }

    public RequestBuilder uri(String uri) {
        this.request.setUri(uri);
        return this;
    }

}
