package com.apiclient.ui;

import com.apiclient.http.HttpRequestService;
import com.apiclient.model.RequestData;
import com.apiclient.model.ResponseData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;

/**
 * Main application window for the API Client.
 */
public class ApiClientFrame extends JFrame {
    private final HttpRequestService httpRequestService;
    
    // UI Components
    private RequestPanel requestPanel;
    private ResponsePanel responsePanel;
    private JLabel statusLabel;
    private JButton sendButton;
    private JProgressBar progressBar;

    public ApiClientFrame() {
        this.httpRequestService = new HttpRequestService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
    }

    private void initializeComponents() {
        // Create main panels
        requestPanel = new RequestPanel();
        responsePanel = new ResponsePanel();
        
        // Status bar components
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.setBackground(new Color(76, 175, 80));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 20));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(sendButton);
        toolBar.addSeparator();
        toolBar.add(progressBar);
        toolBar.add(Box.createHorizontalGlue());

        // Create split pane for request/response
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(requestPanel);
        splitPane.setBottomComponent(responsePanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.4);

        // Add components to frame
        add(toolBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        sendButton.addActionListener(new SendButtonListener());
    }

    private void configureFrame() {
        setTitle("Java API Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
    }

    /**
     * ActionListener for the Send button.
     */
    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendRequest();
        }
    }

    /**
     * Execute the HTTP request.
     */
    private void sendRequest() {
        // Get request data from UI
        RequestData requestData = requestPanel.getRequestData();
        
        // Validate input
        if (requestData.getUrl() == null || requestData.getUrl().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid URL", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update UI state
        setSendingState(true);
        statusLabel.setText("Sending request...");

        // Execute request asynchronously
        CompletableFuture<ResponseData> future = httpRequestService.executeRequestAsync(requestData);
        
        future.thenAccept(responseData -> {
            // Update UI on Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                responsePanel.setResponseData(responseData);
                setSendingState(false);
                
                if (responseData.getStatusCode() == -1) {
                    statusLabel.setText("Request failed: " + responseData.getStatusText());
                } else {
                    statusLabel.setText(String.format("Response: %d %s (%d ms)", 
                        responseData.getStatusCode(), 
                        responseData.getStatusText(),
                        responseData.getResponseTimeMs()));
                }
            });
        }).exceptionally(throwable -> {
            // Handle exceptions
            SwingUtilities.invokeLater(() -> {
                setSendingState(false);
                statusLabel.setText("Request failed: " + throwable.getMessage());
                
                ResponseData errorResponse = new ResponseData();
                errorResponse.setStatusCode(-1);
                errorResponse.setStatusText("Error");
                errorResponse.setBody(throwable.getMessage());
                responsePanel.setResponseData(errorResponse);
            });
            return null;
        });
    }

    /**
     * Update UI state when sending request.
     */
    private void setSendingState(boolean isSending) {
        sendButton.setEnabled(!isSending);
        requestPanel.setEnabled(!isSending);
        progressBar.setVisible(isSending);
        
        if (isSending) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}