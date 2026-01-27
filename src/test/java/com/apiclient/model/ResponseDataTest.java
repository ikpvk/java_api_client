package com.apiclient.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ResponseData class.
 */
class ResponseDataTest {

    private ResponseData responseData;

    @BeforeEach
    void setUp() {
        responseData = new ResponseData();
    }

    @Test
    void testDefaultConstructor_CreatesEmptyObject() {
        // Assert
        assertThat(responseData.getStatusCode()).isEqualTo(0);
        assertThat(responseData.getStatusText()).isNull();
        assertThat(responseData.getBody()).isNull();
        assertThat(responseData.getResponseTimeMs()).isEqualTo(0);
        assertThat(responseData.getHeaders()).isNotNull().isEmpty();
    }

    @Test
    void testParameterizedConstructor_SetsBasicFields() {
        // Arrange
        int statusCode = 200;
        String statusText = "OK";

        // Act
        ResponseData data = new ResponseData(statusCode, statusText);

        // Assert
        assertThat(data.getStatusCode()).isEqualTo(statusCode);
        assertThat(data.getStatusText()).isEqualTo(statusText);
        assertThat(data.getBody()).isEqualTo("");
        assertThat(data.getResponseTimeMs()).isEqualTo(0);
        assertThat(data.getHeaders()).isNotNull().isEmpty();
    }

    @Test
    void testSetAndGetStatusCode_WorksCorrectly() {
        // Arrange
        int statusCode = 404;

        // Act
        responseData.setStatusCode(statusCode);

        // Assert
        assertThat(responseData.getStatusCode()).isEqualTo(statusCode);
    }

    @Test
    void testSetAndGetStatusText_WorksCorrectly() {
        // Arrange
        String statusText = "Not Found";

        // Act
        responseData.setStatusText(statusText);

        // Assert
        assertThat(responseData.getStatusText()).isEqualTo(statusText);
    }

    @Test
    void testSetAndGetBody_WorksCorrectly() {
        // Arrange
        String body = "{\"message\": \"success\"}";

        // Act
        responseData.setBody(body);

        // Assert
        assertThat(responseData.getBody()).isEqualTo(body);
    }

    @Test
    void testSetAndGetResponseTimeMs_WorksCorrectly() {
        // Arrange
        long responseTime = 1234L;

        // Act
        responseData.setResponseTimeMs(responseTime);

        // Assert
        assertThat(responseData.getResponseTimeMs()).isEqualTo(responseTime);
    }

    @Test
    void testSetAndGetHeaders_WorksCorrectly() {
        // Arrange
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Server", "nginx/1.18.0"
        );

        // Act
        responseData.setHeaders(headers);

        // Assert
        assertThat(responseData.getHeaders()).isEqualTo(headers);
    }

    @Test
    void testAddHeader_SingleHeader_AddedCorrectly() {
        // Act
        responseData.addHeader("Content-Type", "application/json");

        // Assert
        assertThat(responseData.getHeaders()).hasSize(1);
        assertThat(responseData.getHeaders()).containsEntry("Content-Type", "application/json");
    }

    @Test
    void testAddHeader_MultipleHeaders_AllAddedCorrectly() {
        // Act
        responseData.addHeader("Content-Type", "application/json");
        responseData.addHeader("Server", "nginx");
        responseData.addHeader("Cache-Control", "no-cache");

        // Assert
        assertThat(responseData.getHeaders()).hasSize(3);
        assertThat(responseData.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(responseData.getHeaders()).containsEntry("Server", "nginx");
        assertThat(responseData.getHeaders()).containsEntry("Cache-Control", "no-cache");
    }

    @Test
    void testAddHeader_ExistingHeader_OverwritesValue() {
        // Arrange
        responseData.addHeader("Content-Type", "application/json");

        // Act
        responseData.addHeader("Content-Type", "text/plain");

        // Assert
        assertThat(responseData.getHeaders()).hasSize(1);
        assertThat(responseData.getHeaders()).containsEntry("Content-Type", "text/plain");
    }

    @Test
    void testIsSuccess_With200_ReturnsTrue() {
        // Arrange
        responseData.setStatusCode(200);

        // Act & Assert
        assertThat(responseData.isSuccess()).isTrue();
    }

    @Test
    void testIsSuccess_With201_ReturnsTrue() {
        // Arrange
        responseData.setStatusCode(201);

        // Act & Assert
        assertThat(responseData.isSuccess()).isTrue();
    }

    @Test
    void testIsSuccess_With204_ReturnsTrue() {
        // Arrange
        responseData.setStatusCode(204);

        // Act & Assert
        assertThat(responseData.isSuccess()).isTrue();
    }

    @Test
    void testIsSuccess_With299_ReturnsTrue() {
        // Arrange
        responseData.setStatusCode(299);

        // Act & Assert
        assertThat(responseData.isSuccess()).isTrue();
    }

    @Test
    void testIsSuccess_With199_ReturnsFalse() {
        // Arrange
        responseData.setStatusCode(199);

        // Act & Assert
        assertThat(responseData.isSuccess()).isFalse();
    }

    @Test
    void testIsSuccess_With400_ReturnsFalse() {
        // Arrange
        responseData.setStatusCode(400);

        // Act & Assert
        assertThat(responseData.isSuccess()).isFalse();
    }

    @Test
    void testIsSuccess_With500_ReturnsFalse() {
        // Arrange
        responseData.setStatusCode(500);

        // Act & Assert
        assertThat(responseData.isSuccess()).isFalse();
    }

    @Test
    void testIsSuccess_With300_ReturnsFalse() {
        // Arrange
        responseData.setStatusCode(300);

        // Act & Assert
        assertThat(responseData.isSuccess()).isFalse();
    }

    @Test
    void testAddHeaderWithNullKey_DoesNotAddHeader() {
        // Act
        responseData.addHeader(null, "value");

        // Assert
        assertThat(responseData.getHeaders()).isEmpty();
    }

    @Test
    void testAddHeaderWithNullValue_AddsHeaderWithNullValue() {
        // Act
        responseData.addHeader("key", null);

        // Assert
        assertThat(responseData.getHeaders()).hasSize(1);
        assertThat(responseData.getHeaders()).containsEntry("key", null);
    }

    @Test
    void testAddHeaderWithEmptyKey_DoesNotAddHeader() {
        // Act
        responseData.addHeader("", "value");

        // Assert
        assertThat(responseData.getHeaders()).isEmpty();
    }

    @Test
    void testAddHeaderWithEmptyValue_AddsHeaderWithEmptyValue() {
        // Act
        responseData.addHeader("key", "");

        // Assert
        assertThat(responseData.getHeaders()).hasSize(1);
        assertThat(responseData.getHeaders()).containsEntry("key", "");
    }

    @Test
    void testHeadersAreCaseSensitive() {
        // Act
        responseData.addHeader("Content-Type", "application/json");
        responseData.addHeader("content-type", "text/plain");

        // Assert
        assertThat(responseData.getHeaders()).hasSize(2);
        assertThat(responseData.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(responseData.getHeaders()).containsEntry("content-type", "text/plain");
    }

    @Test
    void testSetAndGetBody_WithNull_WorksCorrectly() {
        // Act
        responseData.setBody(null);

        // Assert
        assertThat(responseData.getBody()).isNull();
    }

    @Test
    void testSetAndGetBody_WithEmptyString_WorksCorrectly() {
        // Act
        responseData.setBody("");

        // Assert
        assertThat(responseData.getBody()).isEqualTo("");
    }

    @Test
    void testSetAndGetStatusText_WithNull_WorksCorrectly() {
        // Act
        responseData.setStatusText(null);

        // Assert
        assertThat(responseData.getStatusText()).isNull();
    }

    @Test
    void testResponseTimeMs_WithNegativeValue_WorksCorrectly() {
        // Arrange
        long negativeTime = -1L;

        // Act
        responseData.setResponseTimeMs(negativeTime);

        // Assert
        assertThat(responseData.getResponseTimeMs()).isEqualTo(negativeTime);
    }
}