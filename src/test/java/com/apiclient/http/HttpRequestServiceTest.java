package com.apiclient.http;

import com.apiclient.model.RequestData;
import com.apiclient.model.ResponseData;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HttpRequestService class.
 */
class HttpRequestServiceTest {

    private WireMockServer wireMockServer;
    private HttpRequestService httpRequestService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
        httpRequestService = new HttpRequestService();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testGetRequest_ValidUrl_ReturnsResponse() throws Exception {
        // Arrange
        String responseBody = "{\"message\": \"success\"}";
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withStatusMessage("OK")
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/test");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.getBody()).isEqualTo(responseBody);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResponseTimeMs()).isPositive();
    }

    @Test
    void testPostRequest_WithBody_ReturnsResponse() throws Exception {
        // Arrange
        String requestBody = "{\"name\": \"test\"}";
        String responseBody = "{\"id\": 1, \"name\": \"test\"}";
        
        stubFor(post(urlEqualTo("/api/users"))
                .withRequestBody(equalTo(requestBody))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withStatusMessage("Created")
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        RequestData requestData = new RequestData("POST", "http://localhost:8080/api/users");
        requestData.setBody(requestBody);
        requestData.addHeader("Content-Type", "application/json");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getStatusText()).isEqualTo("Created");
        assertThat(response.getBody()).isEqualTo(responseBody);
        assertThat(response.isSuccess()).isTrue();
        
        // Verify request was made correctly
        verify(postRequestedFor(urlEqualTo("/api/users"))
                .withRequestBody(equalTo(requestBody))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    void testPutRequest_WithBody_ReturnsResponse() throws Exception {
        // Arrange
        String requestBody = "{\"name\": \"updated\"}";
        
        stubFor(put(urlEqualTo("/api/users/1"))
                .withRequestBody(equalTo(requestBody))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withStatusMessage("OK")
                        .withBody(requestBody)));

        RequestData requestData = new RequestData("PUT", "http://localhost:8080/api/users/1");
        requestData.setBody(requestBody);

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.getBody()).isEqualTo(requestBody);
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void testDeleteRequest_WithoutBody_ReturnsResponse() throws Exception {
        // Arrange
        stubFor(delete(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withStatusMessage("No Content")));

        RequestData requestData = new RequestData("DELETE", "http://localhost:8080/api/users/1");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(204);
        assertThat(response.getStatusText()).isEqualTo("No Content");
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void testRequest_WithCustomHeaders_SendsHeadersCorrectly() throws Exception {
        // Arrange
        stubFor(get(urlEqualTo("/headers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withStatusMessage("OK")));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/headers");
        requestData.addHeader("Authorization", "Bearer token123");
        requestData.addHeader("User-Agent", "TestClient/1.0");
        requestData.addHeader("Accept", "application/json");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        
        // Verify headers were sent
        verify(getRequestedFor(urlEqualTo("/headers"))
                .withHeader("Authorization", equalTo("Bearer token123"))
                .withHeader("User-Agent", equalTo("TestClient/1.0"))
                .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void testRequest_With404Status_ReturnsErrorResponse() throws Exception {
        // Arrange
        stubFor(get(urlEqualTo("/notfound"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withStatusMessage("Not Found")
                        .withBody("{\"error\": \"Resource not found\"}")));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/notfound");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getStatusText()).isEqualTo("Not Found");
        assertThat(response.getBody()).isEqualTo("{\"error\": \"Resource not found\"}");
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    void testRequest_With500Status_ReturnsErrorResponse() throws Exception {
        // Arrange
        stubFor(get(urlEqualTo("/error"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withStatusMessage("Internal Server Error")
                        .withBody("{\"error\": \"Server error\"}")));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/error");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getStatusText()).isEqualTo("Internal Server Error");
        assertThat(response.getBody()).isEqualTo("{\"error\": \"Server error\"}");
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    void testAsyncRequest_CompletesSuccessfully() throws Exception {
        // Arrange
        String responseBody = "{\"async\": \"success\"}";
        stubFor(get(urlEqualTo("/async"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/async");

        // Act
        CompletableFuture<ResponseData> future = httpRequestService.executeRequestAsync(requestData);
        ResponseData response = future.get();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseBody);
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void testRequest_WithoutDefaultUserAgent_AddsUserAgentHeader() throws Exception {
        // Arrange
        stubFor(get(urlEqualTo("/useragent"))
                .willReturn(aResponse()
                        .withStatus(200)));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/useragent");
        // Don't add User-Agent header

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        
        // Verify default User-Agent was added
        verify(getRequestedFor(urlEqualTo("/useragent"))
                .withHeader("User-Agent", equalTo("Java-API-Client/1.0")));
    }

    @Test
    void testRequest_WithCustomUserAgent_UsesCustomUserAgent() throws Exception {
        // Arrange
        stubFor(get(urlEqualTo("/customuseragent"))
                .willReturn(aResponse()
                        .withStatus(200)));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/customuseragent");
        requestData.addHeader("User-Agent", "CustomClient/2.0");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        
        // Verify custom User-Agent was used, not default
        verify(getRequestedFor(urlEqualTo("/customuseragent"))
                .withHeader("User-Agent", equalTo("CustomClient/2.0")));
    }

    @Test
    void testRequest_WithMultipleResponseHeaders_ParsesHeadersCorrectly() throws Exception {
        // Arrange
        stubFor(get(urlEqualTo("/headers-response"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Cache-Control", "no-cache")
                        .withHeader("Server", "nginx/1.18.0")
                        .withHeader("X-Custom-Header", "custom-value")));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/headers-response");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(response.getHeaders()).containsEntry("Cache-Control", "no-cache");
        assertThat(response.getHeaders()).containsEntry("Server", "nginx/1.18.0");
        assertThat(response.getHeaders()).containsEntry("X-Custom-Header", "custom-value");
    }

    @Test
    void testAsyncRequest_WithException_HandlesGracefully() throws Exception {
        // Arrange
        RequestData requestData = new RequestData("GET", "http://invalid-url-that-does-not-exist.com");

        // Act
        CompletableFuture<ResponseData> future = httpRequestService.executeRequestAsync(requestData);

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, () -> future.get());
        assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(exception.getCause().getMessage()).contains("Request failed");
    }

    @Test
    void testRequest_ResponseTimeIsMeasured() throws Exception {
        // Arrange
        stubFor(get(urlEqualTo("/delayed"))
                .willReturn(aResponse()
                        .withFixedDelay(100) // 100ms delay
                        .withStatus(200)));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/delayed");

        // Act
        long startTime = System.currentTimeMillis();
        ResponseData response = httpRequestService.executeRequest(requestData);
        long endTime = System.currentTimeMillis();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponseTimeMs()).isBetween(90L, 200L); // Allow some variance
    }

    @Test
    void testPostRequest_WithoutBody_SendsEmptyBody() throws Exception {
        // Arrange
        stubFor(post(urlEqualTo("/empty-body"))
                .willReturn(aResponse()
                        .withStatus(200)));

        RequestData requestData = new RequestData("POST", "http://localhost:8080/empty-body");
        // Don't set body

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        
        // Verify request was made with empty body
        verify(postRequestedFor(urlEqualTo("/empty-body"))
                .withRequestBody(equalTo("")));
    }

    @Test
    void testRequest_WithUrlContainingSpecialCharacters_WorksCorrectly() throws Exception {
        // Arrange
        String specialCharsPath = "/api/users/123?filter=name%20test&sort=asc";
        stubFor(get(urlPathEqualTo(specialCharsPath))
                .willReturn(aResponse()
                        .withStatus(200)));

        RequestData requestData = new RequestData("GET", "http://localhost:8080" + specialCharsPath);

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
}