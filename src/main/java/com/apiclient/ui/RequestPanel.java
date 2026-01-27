package com.apiclient.ui;

import com.apiclient.model.RequestData;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel for configuring HTTP requests.
 */
public class RequestPanel extends JPanel {
    private JComboBox<String> methodComboBox;
    private JTextField urlTextField;
    private JTable headersTable;
    private DefaultTableModel headersTableModel;
    private JTextArea bodyTextArea;
    private JScrollPane bodyScrollPane;

    public RequestPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        // HTTP Method selector
        String[] methods = {"GET", "POST", "PUT", "DELETE"};
        methodComboBox = new JComboBox<>(methods);
        methodComboBox.setPreferredSize(new Dimension(80, 25));

        // URL input
        urlTextField = new JTextField();
        urlTextField.setPreferredSize(new Dimension(400, 25));

        // Headers table
        String[] columnNames = {"Header Name", "Value"};
        headersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        headersTable = new JTable(headersTableModel);
        headersTable.getTableHeader().setReorderingAllowed(false);
        headersTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        headersTable.getColumnModel().getColumn(1).setPreferredWidth(250);

        // Add some default headers
        headersTableModel.addRow(new Object[]{"Content-Type", "application/json"});

        // Headers scroll pane
        JScrollPane headersScrollPane = new JScrollPane(headersTable);
        headersScrollPane.setPreferredSize(new Dimension(400, 120));

        // Request body area
        bodyTextArea = new JTextArea();
        bodyTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        bodyTextArea.setLineWrap(false);
        bodyScrollPane = new JScrollPane(bodyTextArea);
        bodyScrollPane.setPreferredSize(new Dimension(400, 150));

        // Initially disable body for GET requests
        updateBodyState();
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Method and URL row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        add(new JLabel("Method:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.0;
        add(methodComboBox, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        add(new JLabel("URL:"), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1.0;
        add(urlTextField, gbc);

        // Headers section
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        JPanel headersPanel = new JPanel(new BorderLayout());
        headersPanel.setBorder(new TitledBorder("Headers"));
        headersPanel.add(new JScrollPane(headersTable), BorderLayout.CENTER);
        
        // Headers buttons panel
        JPanel headersButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addHeaderButton = new JButton("Add Header");
        JButton removeHeaderButton = new JButton("Remove Selected");
        
        addHeaderButton.addActionListener(e -> {
            headersTableModel.addRow(new Object[]{"", ""});
            // Scroll to the new row
            int lastRow = headersTableModel.getRowCount() - 1;
            headersTable.scrollRectToVisible(headersTable.getCellRect(lastRow, 0, true));
            headersTable.editCellAt(lastRow, 0);
        });
        
        removeHeaderButton.addActionListener(e -> {
            int selectedRow = headersTable.getSelectedRow();
            if (selectedRow >= 0) {
                headersTableModel.removeRow(selectedRow);
            }
        });
        
        headersButtonsPanel.add(addHeaderButton);
        headersButtonsPanel.add(removeHeaderButton);
        headersPanel.add(headersButtonsPanel, BorderLayout.SOUTH);
        
        add(headersPanel, gbc);

        // Request body section
        gbc.gridy = 2; gbc.weightx = 1.0; gbc.weighty = 1.0;
        
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(new TitledBorder("Request Body"));
        bodyPanel.add(bodyScrollPane, BorderLayout.CENTER);
        add(bodyPanel, gbc);
    }

    private void setupEventHandlers() {
        methodComboBox.addActionListener(e -> updateBodyState());
    }

    

    /**
     * Enable/disable request body based on HTTP method.
     */
    private void updateBodyState() {
        String method = (String) methodComboBox.getSelectedItem();
        boolean hasBody = !"GET".equals(method) && !"DELETE".equals(method);
        bodyTextArea.setEnabled(hasBody);
        bodyScrollPane.setEnabled(hasBody);
        bodyTextArea.setBackground(hasBody ? Color.WHITE : Color.LIGHT_GRAY);
    }

    /**
     * Get the request data from UI components.
     */
    public RequestData getRequestData() {
        RequestData requestData = new RequestData();
        requestData.setMethod((String) methodComboBox.getSelectedItem());
        requestData.setUrl(urlTextField.getText().trim());
        
        // Add headers from table
        for (int i = 0; i < headersTableModel.getRowCount(); i++) {
            String key = (String) headersTableModel.getValueAt(i, 0);
            String value = (String) headersTableModel.getValueAt(i, 1);
            if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                requestData.addHeader(key.trim(), value.trim());
            }
        }
        
        // Set body if method supports it
        String method = (String) methodComboBox.getSelectedItem();
        if (!"GET".equals(method) && !"DELETE".equals(method)) {
            requestData.setBody(bodyTextArea.getText());
        }
        
        return requestData;
    }

    /**
     * Enable or disable all components in this panel.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        methodComboBox.setEnabled(enabled);
        urlTextField.setEnabled(enabled);
        headersTable.setEnabled(enabled);
        bodyTextArea.setEnabled(enabled && !"GET".equals(methodComboBox.getSelectedItem()) 
                                       && !"DELETE".equals(methodComboBox.getSelectedItem()));
    }
}