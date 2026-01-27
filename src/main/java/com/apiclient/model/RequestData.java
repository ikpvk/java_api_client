package com.apiclient.model;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents the data for an HTTP request.
 */
public class RequestData {
    private String method;
    private String url;
    private Map<String, String> headers;
    private String body;

    public RequestData() {
        this.headers = new HashMap<>();
    }

    public RequestData(String method, String url) {
        this.method = method;
        this.url = url;
        this.headers = new HashMap<>();
        this.body = "";
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void removeHeader(String key) {
        this.headers.remove(key);
    }

    public boolean hasBody() {
        return body != null && !body.trim().isEmpty();
    }
}