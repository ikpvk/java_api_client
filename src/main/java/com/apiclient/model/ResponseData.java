package com.apiclient.model;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents the data for an HTTP response.
 */
public class ResponseData {
    private int statusCode;
    private String statusText;
    private Map<String, String> headers;
    private String body;
    private long responseTimeMs;

    public ResponseData() {
        this.headers = new HashMap<>();
        this.responseTimeMs = 0;
    }

    public ResponseData(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = new HashMap<>();
        this.body = "";
        this.responseTimeMs = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}