package com.apiclient.ui;

import com.apiclient.model.ResponseData;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

/**
 * Panel for displaying HTTP responses.
 */
public class ResponsePanel extends JPanel {
    private JLabel statusLabel;
    private JTextArea headersTextArea;
    private JTextArea bodyTextArea;
    private JScrollPane headersScrollPane;
    private JScrollPane bodyScrollPane;
    private JTabbedPane tabbedPane;

    public ResponsePanel() {
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        // Status label
        statusLabel = new JLabel("No response yet");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Headers display
        headersTextArea = new JTextArea();
        headersTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        headersTextArea.setEditable(false);
        headersTextArea.setBackground(Color.WHITE);
        headersScrollPane = new JScrollPane(headersTextArea);
        headersScrollPane.setPreferredSize(new Dimension(400, 100));

        // Response body display
        bodyTextArea = new JTextArea();
        bodyTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        bodyTextArea.setEditable(false);
        bodyTextArea.setBackground(Color.WHITE);
        bodyTextArea.setLineWrap(false);
        bodyScrollPane = new JScrollPane(bodyTextArea);
        bodyScrollPane.setPreferredSize(new Dimension(400, 200));

        // Create tabbed pane for different views
        tabbedPane = new JTabbedPane();
        tabbedPane.add("Raw", bodyScrollPane);
        
        // Pretty print tab (placeholder for future enhancement)
        JTextArea prettyTextArea = new JTextArea();
        prettyTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        prettyTextArea.setEditable(false);
        prettyTextArea.setBackground(Color.WHITE);
        tabbedPane.add("Pretty", new JScrollPane(prettyTextArea));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Response"));

        // Top panel for status
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(statusLabel, BorderLayout.WEST);
        
        // Headers panel
        JPanel headersPanel = new JPanel(new BorderLayout());
        headersPanel.setBorder(new TitledBorder("Headers"));
        headersPanel.add(headersScrollPane, BorderLayout.CENTER);
        
        // Main content with tabs
        add(topPanel, BorderLayout.NORTH);
        add(headersPanel, BorderLayout.CENTER);
        
        // Response body panel
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(new TitledBorder("Body"));
        bodyPanel.add(tabbedPane, BorderLayout.CENTER);
        bodyPanel.setPreferredSize(new Dimension(400, 250));
        
        // Combine headers and body
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(headersPanel);
        splitPane.setBottomComponent(bodyPanel);
        splitPane.setDividerLocation(150);
        splitPane.setResizeWeight(0.4);
        
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Update the panel with response data.
     */
    public void setResponseData(ResponseData responseData) {
        if (responseData == null) {
            clearResponse();
            return;
        }

        // Update status
        String statusText = responseData.getStatusCode() + " " + responseData.getStatusText();
        statusLabel.setText(statusText);
        
        // Color code status
        if (responseData.getStatusCode() >= 200 && responseData.getStatusCode() < 300) {
            statusLabel.setForeground(new Color(0, 128, 0)); // Green for success
        } else if (responseData.getStatusCode() >= 400) {
            statusLabel.setForeground(new Color(200, 0, 0)); // Red for client errors
        } else if (responseData.getStatusCode() >= 500) {
            statusLabel.setForeground(new Color(200, 0, 0)); // Red for server errors
        } else {
            statusLabel.setForeground(new Color(0, 0, 200)); // Blue for informational
        }

        // Update headers
        StringBuilder headersText = new StringBuilder();
        for (Map.Entry<String, String> header : responseData.getHeaders().entrySet()) {
            headersText.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
        }
        headersTextArea.setText(headersText.toString());
        headersTextArea.setCaretPosition(0);

        // Update body
        String body = responseData.getBody();
        if (body == null || body.trim().isEmpty()) {
            bodyTextArea.setText("(No response body)");
        } else {
            bodyTextArea.setText(body);
        }
        bodyTextArea.setCaretPosition(0);

        // Update pretty view (basic JSON pretty printing)
        JTextArea prettyTextArea = (JTextArea) ((JScrollPane) tabbedPane.getComponentAt(1)).getViewport().getView();
        if (body != null && !body.trim().isEmpty() && isJsonResponse(responseData)) {
            try {
                String prettyJson = prettyPrintJson(body);
                prettyTextArea.setText(prettyJson);
            } catch (Exception e) {
                prettyTextArea.setText("Could not format JSON: " + e.getMessage());
            }
        } else {
            prettyTextArea.setText(body != null ? body : "(No response body)");
        }
        prettyTextArea.setCaretPosition(0);
    }

    /**
     * Clear the response display.
     */
    public void clearResponse() {
        statusLabel.setText("No response yet");
        statusLabel.setForeground(Color.BLACK);
        headersTextArea.setText("");
        bodyTextArea.setText("(No response yet)");
        
        JTextArea prettyTextArea = (JTextArea) ((JScrollPane) tabbedPane.getComponentAt(1)).getViewport().getView();
        prettyTextArea.setText("(No response yet)");
    }

    /**
     * Check if the response is likely JSON based on headers.
     */
    private boolean isJsonResponse(ResponseData responseData) {
        String contentType = responseData.getHeaders().get("Content-Type");
        if (contentType == null) {
            contentType = responseData.getHeaders().get("content-type");
        }
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    /**
     * Simple JSON pretty printing (basic implementation).
     */
    private String prettyPrintJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }

        StringBuilder pretty = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        boolean escapeNext = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escapeNext) {
                pretty.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                pretty.append(c);
                escapeNext = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                pretty.append(c);
                continue;
            }

            if (inString) {
                pretty.append(c);
                continue;
            }

            switch (c) {
                case '{':
                case '[':
                    pretty.append(c);
                    indent++;
                    pretty.append('\n');
                    addIndent(pretty, indent);
                    break;
                case '}':
                case ']':
                    pretty.append('\n');
                    indent--;
                    addIndent(pretty, indent);
                    pretty.append(c);
                    break;
                case ',':
                    pretty.append(c);
                    pretty.append('\n');
                    addIndent(pretty, indent);
                    break;
                case ':':
                    pretty.append(c);
                    pretty.append(' ');
                    break;
                default:
                    if (!Character.isWhitespace(c)) {
                        pretty.append(c);
                    }
                    break;
            }
        }

        return pretty.toString();
    }

    /**
     * Add indentation to the string builder.
     */
    private void addIndent(StringBuilder sb, int level) {
        for (int i = 0; i < level * 2; i++) {
            sb.append(' ');
        }
    }
}