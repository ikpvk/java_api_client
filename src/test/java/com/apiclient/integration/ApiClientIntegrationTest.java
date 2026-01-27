package com.apiclient.integration;

import com.apiclient.http.HttpRequestService;
import com.apiclient.model.RequestData;
import com.apiclient.model.ResponseData;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the complete API client workflow.
 */
class ApiClientIntegrationTest {

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
    void testCompleteWorkflow_GetRequest_Success() throws Exception {
        // Arrange
        String responseBody = "{\n" +
                "  \"userId\": 1,\n" +
                "  \"id\": 1,\n" +
                "  \"title\": \"delectus aut autem\",\n" +
                "  \"completed\": false\n" +
                "}";
        
        stubFor(get(urlEqualTo("/todos/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withStatusMessage("OK")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Server", "nginx/1.18.0")
                        .withBody(responseBody)));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/todos/1");
        requestData.addHeader("Accept", "application/json");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert - Verify complete response
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getBody()).isEqualTo(responseBody);
        assertThat(response.getResponseTimeMs()).isPositive();
        
        // Verify headers
        assertThat(response.getHeaders()).hasSize(3); // Content-Type, Server, Date (added by server)
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(response.getHeaders()).containsEntry("Server", "nginx/1.18.0");
        
        // Verify request was made correctly
        verify(getRequestedFor(urlEqualTo("/todos/1"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("User-Agent", equalTo("Java-API-Client/1.0")));
    }

    @Test
    void testCompleteWorkflow_PostRequest_WithBody() throws Exception {
        // Arrange
        String requestBody = "{\n" +
                "  \"title\": \"foo\",\n" +
                "  \"body\": \"bar\",\n" +
                "  \"userId\": 1\n" +
                "}";
        String responseBody = "{\n" +
                "  \"title\": \"foo\",\n" +
                "  \"body\": \"bar\",\n" +
                "  \"userId\": 1,\n" +
                "  \"id\": 201\n" +
                "}";

        stubFor(post(urlEqualTo("/posts"))
                .withRequestBody(equalTo(requestBody))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withStatusMessage("Created")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/posts/201")
                        .withBody(responseBody)));

        RequestData requestData = new RequestData("POST", "http://localhost:8080/posts");
        requestData.setBody(requestBody);
        requestData.addHeader("Content-Type", "application/json");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getStatusText()).isEqualTo("Created");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getBody()).isEqualTo(responseBody);
        
        // Verify headers
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(response.getHeaders()).containsEntry("Location", "/posts/201");
        
        // Verify request
        verify(postRequestedFor(urlEqualTo("/posts"))
                .withRequestBody(equalTo(requestBody))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    void testCompleteWorkflow_ErrorResponse_HandlesGracefully() throws Exception {
        // Arrange
        String errorBody = "{\n" +
                "  \"error\": \"Validation failed\",\n" +
                "  \"details\": {\n" +
                "    \"field\": \"title\",\n" +
                "    \"message\": \"Title is required\"\n" +
                "  }\n" +
                "}";

        stubFor(post(urlEqualTo("/error"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withStatusMessage("Bad Request")
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorBody)));

        RequestData requestData = new RequestData("POST", "http://localhost:8080/error");
        requestData.setBody("{\"invalid\": \"data\"}");
        requestData.addHeader("Content-Type", "application/json");

        // Act
        ResponseData response = httpRequestService.executeRequest(requestData);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getStatusText()).isEqualTo("Bad Request");
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void testCompleteWorkflow_AsyncRequest_WorksCorrectly() throws Exception {
        // Arrange
        String responseBody = "\"async response\"";
        
        stubFor(get(urlEqualTo("/async"))
                .willReturn(aResponse()
                        .withFixedDelay(100) // Simulate async delay
                        .withStatus(200)
                        .withBody(responseBody)));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/async");

        // Act
        CompletableFuture<ResponseData> future = httpRequestService.executeRequestAsync(requestData);
        ResponseData response = future.get();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getBody()).isEqualTo(responseBody);
        assertThat(response.getResponseTimeMs()).isBetween(90L, 200L); // Account for delay
    }

    @Test
    void testCompleteWorkflow_TimedRequest_MeasuresCorrectly() throws Exception {
        // Arrange
        stubFor(get(urlEqualTo("/timed"))
                .willReturn(aResponse()
                        .withFixedDelay(250) // 250ms delay
                        .withStatus(200)
                        .withBody("\"timed response\"")));

        RequestData requestData = new RequestData("GET", "http://localhost:8080/timed");

        // Act
        long startTime = System.currentTimeMillis();
        ResponseData response = httpRequestService.executeRequest(requestData);
        long endTime = System.currentTimeMillis();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getBody()).isEqualTo("\"timed response\"");
        assertThat(response.getResponseTimeMs()).isBetween(240L, 300L); // Account for variance
        
        // Verify our timing is reasonable
        long actualDuration = endTime - startTime;
        assertThat(Math.abs(actualDuration - response.getResponseTimeMs())).isLessThan(50); // Within 50ms
    }
}