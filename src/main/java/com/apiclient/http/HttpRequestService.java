package com.apiclient.http;

import com.apiclient.model.RequestData;
import com.apiclient.model.ResponseData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for handling HTTP requests using Java 11 HttpClient.
 */
public class HttpRequestService {
    private final HttpClient httpClient;

    public HttpRequestService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Execute an HTTP request asynchronously.
     */
    public CompletableFuture<ResponseData> executeRequestAsync(RequestData requestData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeRequest(requestData);
            } catch (Exception e) {
                throw new RuntimeException("Request failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Execute an HTTP request synchronously.
     */
    public ResponseData executeRequest(RequestData requestData) throws Exception {
        long startTime = System.currentTimeMillis();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(requestData.getUrl()))
                .timeout(Duration.ofSeconds(30));

        // Set HTTP method
        String method = requestData.getMethod().toUpperCase();
        HttpRequest.BodyPublisher bodyPublisher = requestData.hasBody() 
                ? BodyPublishers.ofString(requestData.getBody()) 
                : BodyPublishers.noBody();

        requestBuilder.method(method, bodyPublisher);

        // Add headers
        for (Map.Entry<String, String> header : requestData.getHeaders().entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }

        // Set default User-Agent if not provided
        if (!requestData.getHeaders().containsKey("User-Agent")) {
            requestBuilder.header("User-Agent", "Java-API-Client/1.0");
        }

        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            long responseTime = System.currentTimeMillis() - startTime;

            // Convert to ResponseData
            ResponseData responseData = new ResponseData();
            responseData.setStatusCode(response.statusCode());
            responseData.setStatusText(getStatusText(response.statusCode()));
            responseData.setBody(response.body());
            responseData.setResponseTimeMs(responseTime);

            // Convert headers
            response.headers().map().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    responseData.addHeader(key, String.join(", ", values));
                }
            });

            return responseData;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            ResponseData errorResponse = new ResponseData();
            errorResponse.setStatusCode(-1);
            errorResponse.setStatusText("Error: " + e.getMessage());
            errorResponse.setBody(e.getMessage());
            errorResponse.setResponseTimeMs(responseTime);
            return errorResponse;
        }
    }

    /**
     * Get standard HTTP status text for status codes.
     */
    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 100: return "Continue";
            case 101: return "Switching Protocols";
            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 203: return "Non-Authoritative Information";
            case 204: return "No Content";
            case 205: return "Reset Content";
            case 206: return "Partial Content";
            case 300: return "Multiple Choices";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 303: return "See Other";
            case 304: return "Not Modified";
            case 305: return "Use Proxy";
            case 307: return "Temporary Redirect";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 408: return "Request Timeout";
            case 409: return "Conflict";
            case 410: return "Gone";
            case 429: return "Too Many Requests";
            case 500: return "Internal Server Error";
            case 501: return "Not Implemented";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            default: return "Unknown Status";
        }
    }
}