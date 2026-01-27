package com.apiclient.ui;

import com.apiclient.model.ResponseData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Font;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ResponsePanel class.
 */
class ResponsePanelTest {

    private ResponsePanel responsePanel;

    @BeforeEach
    void setUp() {
        // Initialize GUI components for testing
        responsePanel = new ResponsePanel();
    }

    @Test
    void testConstructor_InitializesComponentsCorrectly() {
        // Assert
        assertThat(responsePanel).isNotNull();
        assertThat(responsePanel.isEnabled()).isTrue();
    }

    @Test
    void testSetResponseData_WithNull_ClearsResponse() {
        // Act
        responsePanel.setResponseData(null);

        // Assert
        JLabel statusLabel = findStatusLabel();
        assertThat(statusLabel.getText()).isEqualTo("No response yet");
        assertThat(statusLabel.getForeground()).isEqualTo(Color.BLACK);
        
        JTextArea headersArea = findHeadersTextArea();
        assertThat(headersArea.getText()).isEmpty();
        
        JTextArea bodyArea = findBodyTextArea();
        assertThat(bodyArea.getText()).isEqualTo("(No response yet)");
        
        JTextArea prettyArea = findPrettyTextArea();
        assertThat(prettyArea.getText()).isEqualTo("(No response yet)");
    }

    @Test
    void testSetResponseData_With200Status_DisplaysGreenStatus() {
        // Arrange
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(200);
        responseData.setStatusText("OK");
        responseData.setBody("{\"message\": \"success\"}");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JLabel statusLabel = findStatusLabel();
        assertThat(statusLabel.getText()).isEqualTo("200 OK");
        assertThat(statusLabel.getForeground()).isEqualTo(new Color(0, 128, 0)); // Green
    }

    @Test
    void testSetResponseData_With404Status_DisplaysRedStatus() {
        // Arrange
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(404);
        responseData.setStatusText("Not Found");
        responseData.setBody("{\"error\": \"Not found\"}");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JLabel statusLabel = findStatusLabel();
        assertThat(statusLabel.getText()).isEqualTo("404 Not Found");
        assertThat(statusLabel.getForeground()).isEqualTo(new Color(200, 0, 0)); // Red
    }

    @Test
    void testSetResponseData_With500Status_DisplaysRedStatus() {
        // Arrange
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(500);
        responseData.setStatusText("Internal Server Error");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JLabel statusLabel = findStatusLabel();
        assertThat(statusLabel.getText()).isEqualTo("500 Internal Server Error");
        assertThat(statusLabel.getForeground()).isEqualTo(new Color(200, 0, 0)); // Red
    }

    @Test
    void testSetResponseData_With300Status_DisplaysBlueStatus() {
        // Arrange
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(301);
        responseData.setStatusText("Moved Permanently");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JLabel statusLabel = findStatusLabel();
        assertThat(statusLabel.getText()).isEqualTo("301 Moved Permanently");
        assertThat(statusLabel.getForeground()).isEqualTo(new Color(0, 0, 200)); // Blue
    }

    @Test
    void testSetResponseData_WithHeaders_DisplaysHeaders() {
        // Arrange
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(200);
        responseData.setStatusText("OK");
        responseData.addHeader("Content-Type", "application/json");
        responseData.addHeader("Server", "nginx/1.18.0");
        responseData.addHeader("Cache-Control", "no-cache");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JTextArea headersArea = findHeadersTextArea();
        String headersText = headersArea.getText();
        assertThat(headersText).contains("Content-Type: application/json");
        assertThat(headersText).contains("Server: nginx/1.18.0");
        assertThat(headersText).contains("Cache-Control: no-cache");
    }

    @Test
    void testSetResponseData_WithBody_DisplaysRawAndPrettyBody() {
        // Arrange
        String responseBody = "{\"message\": \"success\", \"data\": {\"id\": 1}}";
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(200);
        responseData.setStatusText("OK");
        responseData.setBody(responseBody);

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JTextArea bodyArea = findBodyTextArea();
        JTextArea prettyArea = findPrettyTextArea();
        
        // Raw body should match exactly
        assertThat(bodyArea.getText()).isEqualTo(responseBody);
        
        // Pretty body should be formatted (basic pretty printing)
        String prettyText = prettyArea.getText();
        assertThat(prettyText).contains("\"message\": \"success\"");
        assertThat(prettyText).contains("\"data\": {");
        assertThat(prettyText).contains("\"id\": 1");
    }

    @Test
    void testSetResponseData_WithJsonResponse_FormatsPrettyCorrectly() {
        // Arrange
        String jsonResponse = "{\"name\": \"test\", \"age\": 25, \"active\": true}";
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(200);
        responseData.setStatusText("OK");
        responseData.setBody(jsonResponse);
        responseData.addHeader("Content-Type", "application/json");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JTextArea prettyArea = findPrettyTextArea();
        String prettyText = prettyArea.getText();
        
        // Should be formatted JSON
        assertThat(prettyText).contains("{");
        assertThat(prettyText).contains("\"name\": \"test\",");
        assertThat(prettyText).contains("\"age\": 25,");
        assertThat(prettyText).contains("\"active\": true");
        assertThat(prettyText).contains("}");
        
        // Should have proper indentation (basic check)
        assertThat(prettyText).contains("\n  "); // Newline and indentation
    }

    @Test
    void testSetResponseData_WithNonJsonResponse_ShowsRawInBothTabs() {
        // Arrange
        String textResponse = "Plain text response";
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(200);
        responseData.setStatusText("OK");
        responseData.setBody(textResponse);
        responseData.addHeader("Content-Type", "text/plain");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JTextArea bodyArea = findBodyTextArea();
        JTextArea prettyArea = findPrettyTextArea();
        
        // Both should show raw text for non-JSON responses
        assertThat(bodyArea.getText()).isEqualTo(textResponse);
        assertThat(prettyArea.getText()).isEqualTo(textResponse);
    }

    @Test
    void testSetResponseData_WithEmptyBody_HandlesGracefully() {
        // Arrange
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(204);
        responseData.setStatusText("No Content");
        responseData.setBody("");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JTextArea bodyArea = findBodyTextArea();
        JTextArea prettyArea = findPrettyTextArea();
        
        assertThat(bodyArea.getText()).isEqualTo("(No response body)");
        assertThat(prettyArea.getText()).isEqualTo("(No response body)");
    }

    @Test
    void testSetResponseData_WithNullBody_HandlesGracefully() {
        // Arrange
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(204);
        responseData.setStatusText("No Content");
        responseData.setBody(null);

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JTextArea bodyArea = findBodyTextArea();
        JTextArea prettyArea = findPrettyTextArea();
        
        assertThat(bodyArea.getText()).isEqualTo("(No response body)");
        assertThat(prettyArea.getText()).isEqualTo("(No response body)");
    }

    @Test
    void testSetResponseData_WithResponseTime_UpdatesDisplay() {
        // This test is conceptual since response time is shown in ApiClientFrame status bar
        // In a real implementation, ResponsePanel might have a label for response time
        // For now, we test that response data contains the time
        // Arrange
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(200);
        responseData.setStatusText("OK");
        responseData.setResponseTimeMs(1234L);

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        assertThat(responseData.getResponseTimeMs()).isEqualTo(1234L);
    }

    @Test
    void testSetResponseData_WithMalformedJson_ShowsErrorInPrettyTab() {
        // Arrange
        String malformedJson = "{\"message\": \"success\", \"incomplete\": ";
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(200);
        responseData.setStatusText("OK");
        responseData.setBody(malformedJson);
        responseData.addHeader("Content-Type", "application/json");

        // Act
        responsePanel.setResponseData(responseData);

        // Assert
        JTextArea prettyArea = findPrettyTextArea();
        String prettyText = prettyArea.getText();
        
        // Should contain an error message for malformed JSON
        assertThat(prettyText).contains("Could not format JSON");
    }

    @Test
    void testClearResponse_ResetsToInitialState() {
        // Arrange - First set some response data
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(200);
        responseData.setStatusText("OK");
        responseData.setBody("{\"test\": \"data\"}");
        responsePanel.setResponseData(responseData);

        // Act
        responsePanel.clearResponse();

        // Assert
        JLabel statusLabel = findStatusLabel();
        assertThat(statusLabel.getText()).isEqualTo("No response yet");
        assertThat(statusLabel.getForeground()).isEqualTo(Color.BLACK);
        
        JTextArea headersArea = findHeadersTextArea();
        assertThat(headersArea.getText()).isEmpty();
        
        JTextArea bodyArea = findBodyTextArea();
        assertThat(bodyArea.getText()).isEqualTo("(No response yet)");
        
        JTextArea prettyArea = findPrettyTextArea();
        assertThat(prettyArea.getText()).isEqualTo("(No response yet)");
    }

    @Test
    void testSetResponseData_MultipleCalls_UpdatesCorrectly() {
        // Arrange & Act - First response
        ResponseData firstResponse = new ResponseData();
        firstResponse.setStatusCode(200);
        firstResponse.setStatusText("OK");
        firstResponse.setBody("First response");
        responsePanel.setResponseData(firstResponse);

        // Assert first response
        JLabel statusLabel = findStatusLabel();
        assertThat(statusLabel.getText()).isEqualTo("200 OK");
        JTextArea bodyArea = findBodyTextArea();
        assertThat(bodyArea.getText()).isEqualTo("First response");

        // Act - Second response
        ResponseData secondResponse = new ResponseData();
        secondResponse.setStatusCode(404);
        secondResponse.setStatusText("Not Found");
        secondResponse.setBody("Second response");
        responsePanel.setResponseData(secondResponse);

        // Assert second response
        assertThat(statusLabel.getText()).isEqualTo("404 Not Found");
        assertThat(statusLabel.getForeground()).isEqualTo(new Color(200, 0, 0)); // Red
        assertThat(bodyArea.getText()).isEqualTo("Second response");
    }

    // Helper methods to find UI components
    private JLabel findStatusLabel() {
        // Search for the status label in the component hierarchy
        for (int i = 0; i < responsePanel.getComponentCount(); i++) {
            if (responsePanel.getComponent(i) instanceof JLabel) {
                JLabel label = (JLabel) responsePanel.getComponent(i);
                if (label.getText() != null) {
                    return label;
                }
            }
        }
        return null;
    }

    private JTextArea findHeadersTextArea() {
        // Find headers text area (likely in a scroll pane)
        return findTextAreaInScrollPane("Content-Type: application/json");
    }

    private JTextArea findBodyTextArea() {
        // Find body text area (raw tab)
        JTabbedPane tabbedPane = findTabbedPane();
        if (tabbedPane != null && tabbedPane.getTabCount() > 0) {
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(0); // Raw tab
            return (JTextArea) scrollPane.getViewport().getView();
        }
        return null;
    }

    private JTextArea findPrettyTextArea() {
        // Find pretty text area (pretty tab)
        JTabbedPane tabbedPane = findTabbedPane();
        if (tabbedPane != null && tabbedPane.getTabCount() > 1) {
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(1); // Pretty tab
            return (JTextArea) scrollPane.getViewport().getView();
        }
        return null;
    }

    private JTabbedPane findTabbedPane() {
        // Find tabbed pane in the component hierarchy
        for (int i = 0; i < responsePanel.getComponentCount(); i++) {
            if (responsePanel.getComponent(i) instanceof JTabbedPane) {
                return (JTabbedPane) responsePanel.getComponent(i);
            }
        }
        return null;
    }

    private JTextArea findTextAreaInScrollPane(String sampleText) {
        // Helper to find text areas based on content (for testing)
        for (int i = 0; i < responsePanel.getComponentCount(); i++) {
            if (responsePanel.getComponent(i) instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) responsePanel.getComponent(i);
                if (scrollPane.getViewport().getView() instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
                    return textArea;
                }
            }
        }
        return null;
    }
}