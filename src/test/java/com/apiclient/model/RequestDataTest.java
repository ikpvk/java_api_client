package com.apiclient.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RequestData class.
 */
class RequestDataTest {

    private RequestData requestData;

    @BeforeEach
    void setUp() {
        requestData = new RequestData();
    }

    @Test
    void testDefaultConstructor_CreatesEmptyObject() {
        // Assert
        assertThat(requestData.getMethod()).isNull();
        assertThat(requestData.getUrl()).isNull();
        assertThat(requestData.getBody()).isNull();
        assertThat(requestData.getHeaders()).isNotNull().isEmpty();
    }

    @Test
    void testParameterizedConstructor_SetsBasicFields() {
        // Arrange
        String method = "POST";
        String url = "https://api.example.com/test";

        // Act
        RequestData data = new RequestData(method, url);

        // Assert
        assertThat(data.getMethod()).isEqualTo(method);
        assertThat(data.getUrl()).isEqualTo(url);
        assertThat(data.getBody()).isEqualTo("");
        assertThat(data.getHeaders()).isNotNull().isEmpty();
    }

    @Test
    void testSetAndGetMethod_WorksCorrectly() {
        // Arrange
        String method = "PUT";

        // Act
        requestData.setMethod(method);

        // Assert
        assertThat(requestData.getMethod()).isEqualTo(method);
    }

    @Test
    void testSetAndGetUrl_WorksCorrectly() {
        // Arrange
        String url = "https://api.example.com/users";

        // Act
        requestData.setUrl(url);

        // Assert
        assertThat(requestData.getUrl()).isEqualTo(url);
    }

    @Test
    void testSetAndGetBody_WorksCorrectly() {
        // Arrange
        String body = "{\"name\": \"test\"}";

        // Act
        requestData.setBody(body);

        // Assert
        assertThat(requestData.getBody()).isEqualTo(body);
    }

    @Test
    void testSetAndGetHeaders_WorksCorrectly() {
        // Arrange
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Authorization", "Bearer token123"
        );

        // Act
        requestData.setHeaders(headers);

        // Assert
        assertThat(requestData.getHeaders()).isEqualTo(headers);
    }

    @Test
    void testAddHeader_SingleHeader_AddedCorrectly() {
        // Act
        requestData.addHeader("Accept", "application/json");

        // Assert
        assertThat(requestData.getHeaders()).hasSize(1);
        assertThat(requestData.getHeaders()).containsEntry("Accept", "application/json");
    }

    @Test
    void testAddHeader_MultipleHeaders_AllAddedCorrectly() {
        // Act
        requestData.addHeader("Content-Type", "application/json");
        requestData.addHeader("Authorization", "Bearer token");
        requestData.addHeader("User-Agent", "TestClient/1.0");

        // Assert
        assertThat(requestData.getHeaders()).hasSize(3);
        assertThat(requestData.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(requestData.getHeaders()).containsEntry("Authorization", "Bearer token");
        assertThat(requestData.getHeaders()).containsEntry("User-Agent", "TestClient/1.0");
    }

    @Test
    void testAddHeader_ExistingHeader_OverwritesValue() {
        // Arrange
        requestData.addHeader("Accept", "application/json");

        // Act
        requestData.addHeader("Accept", "text/plain");

        // Assert
        assertThat(requestData.getHeaders()).hasSize(1);
        assertThat(requestData.getHeaders()).containsEntry("Accept", "text/plain");
    }

    @Test
    void testRemoveHeader_ExistingHeader_RemovesCorrectly() {
        // Arrange
        requestData.addHeader("Accept", "application/json");
        requestData.addHeader("Authorization", "Bearer token");

        // Act
        requestData.removeHeader("Authorization");

        // Assert
        assertThat(requestData.getHeaders()).hasSize(1);
        assertThat(requestData.getHeaders()).containsEntry("Accept", "application/json");
        assertThat(requestData.getHeaders()).doesNotContainKey("Authorization");
    }

    @Test
    void testRemoveHeader_NonExistingHeader_NoChange() {
        // Arrange
        requestData.addHeader("Accept", "application/json");

        // Act
        requestData.removeHeader("Authorization");

        // Assert
        assertThat(requestData.getHeaders()).hasSize(1);
        assertThat(requestData.getHeaders()).containsEntry("Accept", "application/json");
    }

    @Test
    void testHasBody_WithValidBody_ReturnsTrue() {
        // Arrange
        requestData.setBody("{\"test\": \"data\"}");

        // Act & Assert
        assertThat(requestData.hasBody()).isTrue();
    }

    @Test
    void testHasBody_WithWhitespaceBody_ReturnsFalse() {
        // Arrange
        requestData.setBody("   ");

        // Act & Assert
        assertThat(requestData.hasBody()).isFalse();
    }

    @Test
    void testHasBody_WithEmptyBody_ReturnsFalse() {
        // Arrange
        requestData.setBody("");

        // Act & Assert
        assertThat(requestData.hasBody()).isFalse();
    }

    @Test
    void testHasBody_WithNullBody_ReturnsFalse() {
        // Act & Assert
        assertThat(requestData.hasBody()).isFalse();
    }

    @Test
    void testHasBody_WithNonEmptyString_ReturnsTrue() {
        // Arrange
        requestData.setBody("some content");

        // Act & Assert
        assertThat(requestData.hasBody()).isTrue();
    }

    @Test
    void testHeadersAreCaseSensitive() {
        // Act
        requestData.addHeader("Content-Type", "application/json");
        requestData.addHeader("content-type", "text/plain");

        // Assert
        assertThat(requestData.getHeaders()).hasSize(2);
        assertThat(requestData.getHeaders()).containsEntry("Content-Type", "application/json");
        assertThat(requestData.getHeaders()).containsEntry("content-type", "text/plain");
    }

    @Test
    void testAddHeaderWithNullKey_DoesNotAddHeader() {
        // Act
        requestData.addHeader(null, "value");

        // Assert
        assertThat(requestData.getHeaders()).isEmpty();
    }

    @Test
    void testAddHeaderWithNullValue_AddsHeaderWithNullValue() {
        // Act
        requestData.addHeader("key", null);

        // Assert
        assertThat(requestData.getHeaders()).hasSize(1);
        assertThat(requestData.getHeaders()).containsEntry("key", null);
    }

    @Test
    void testAddHeaderWithEmptyKey_DoesNotAddHeader() {
        // Act
        requestData.addHeader("", "value");

        // Assert
        assertThat(requestData.getHeaders()).isEmpty();
    }

    @Test
    void testAddHeaderWithEmptyValue_AddsHeaderWithEmptyValue() {
        // Act
        requestData.addHeader("key", "");

        // Assert
        assertThat(requestData.getHeaders()).hasSize(1);
        assertThat(requestData.getHeaders()).containsEntry("key", "");
    }
}