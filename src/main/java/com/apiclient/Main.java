package com.apiclient;

import com.apiclient.ui.ApiClientFrame;

import javax.swing.SwingUtilities;

/**
 * Main entry point for the Java API Client application.
 */
public class Main {
    public static void main(String[] args) {
        // Set Look and Feel to system default for better native appearance
        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to default Look and Feel if system default fails
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Create and show the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ApiClientFrame frame = new ApiClientFrame();
            frame.setVisible(true);
        });
    }
}