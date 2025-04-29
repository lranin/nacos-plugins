package com.yifeng.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.JBUI;
import com.yifeng.model.EnvironmentEnums;
import com.yifeng.model.NacosServerConfig;
import com.yifeng.model.ServerName;
import com.yifeng.service.NacosGlobalConfigState;
import com.yifeng.utils.SecurePasswordStorage;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;

public class NacosGlobalConfigurable implements Configurable {
    private JPanel mainPanel;
    private JComboBox<String> serverConfigDropdown;
    private JComboBox<String> environmentDropdown;
    private JTextField serverField;
    private JTextField namespaceField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField exportPathField; // 全局配置的导出路径输入框
    private JButton saveButton;
    private String currentEnvironment = EnvironmentEnums.DEV.getEnv();

    @Override
    public @Nullable JComponent createComponent() {
        mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = createTopPanel();
        JPanel formPanel = createFormPanel();

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        loadSettings();
        return mainPanel;
    }

    private @NotNull JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String[] serverNames = Arrays.stream(ServerName.values())
                .map(ServerName::getServerName)
                .toArray(String[]::new);

        // Server Config Dropdown
        serverConfigDropdown = new ComboBox<>(serverNames);
        serverConfigDropdown.addActionListener(e -> loadSettings());

        // Environment Config Dropdown
        String[] environments = Arrays.stream(EnvironmentEnums.values())
                .map(EnvironmentEnums::name)
                .toArray(String[]::new);

        environmentDropdown = new ComboBox<>(environments);
        environmentDropdown.addActionListener(e -> switchEnvironment(EnvironmentEnums.valueOf((String) environmentDropdown.getSelectedItem())));

        panel.add(new JLabel("Server:"));
        panel.add(serverConfigDropdown);
        panel.add(new JLabel("Environment:"));
        panel.add(environmentDropdown);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(15); // Increased padding for better spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields with increased width
        serverField = new JTextField(30);
        namespaceField = new JTextField(30);
        usernameField = new JTextField("reader", 30);
        passwordField = new JPasswordField(30);
        exportPathField = new JTextField(30);  // Export Path Input Field

        // Save button with improved alignment and styling
        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(100, 30)); // Explicit size for the button

        // Label and field arrangement with improved alignment
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createLabeledField("Server Address:", serverField), gbc);
        gbc.gridy++;
        panel.add(createLabeledField("Username:", usernameField), gbc);
        gbc.gridy++;
        panel.add(createLabeledField("Namespace:", namespaceField), gbc);
        gbc.gridy++;
        panel.add(createLabeledField("Password:", passwordField), gbc);
        gbc.gridy++;
        panel.add(createLabeledField("Export Path:", exportPathField), gbc); // Adding Export Path
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(saveButton, gbc);

        // Button actions
        saveButton.addActionListener(e -> apply());

        return panel;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(120, label.getPreferredSize().height)); // Fixed width for labels to align uniformly
        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void switchEnvironment(EnvironmentEnums environment) {
        currentEnvironment = environment.getEnv();
        loadSettings();
    }

    private void loadSettings() {
        String configDrop = serverConfigDropdown.getSelectedItem().toString();
        String mapKey = configDrop + currentEnvironment;
        NacosServerConfig config = getStringNacosConfigMap().getOrDefault(mapKey, new NacosServerConfig());

        // Load server address
        String serverAddr = config.getServerAddr();
        String defaultServerAddr = ServerName.NORMAL.getServerName().equals(configDrop) ? ServerName.NORMAL.getServerAddr() : ServerName.CLUSTER.getServerAddr();
        serverField.setText(StringUtils.isBlank(serverAddr) ? defaultServerAddr : serverAddr);

        // Load namespace
        String namespace = config.getNamespace();
        if (StringUtils.isBlank(namespace)) {
            namespace = getDefaultNamespaceForEnvironment();
        }
        namespaceField.setText(StringUtils.isBlank(namespace) ? "public" : namespace);

        // Load username
        String username = config.getUsername();
        usernameField.setText(StringUtils.isBlank(username) ? "reader" : username);

        // Load export path from global settings
        String exportPath = NacosGlobalConfigState.getInstance().getExportPath();
        exportPathField.setText(exportPath != null ? exportPath : "");
    }

    private String getDefaultNamespaceForEnvironment() {
        switch (currentEnvironment) {
            case "dev": return "develop";
            case "test": return "testing";
            case "prod": return "product";
            default: return "public";
        }
    }

    private String getMapKey() {
        return serverConfigDropdown.getSelectedItem() + currentEnvironment;
    }

    private Map<String, NacosServerConfig> getStringNacosConfigMap() {
        return NacosGlobalConfigState.getInstance().getConfigMap();
    }

    @Override
    public boolean isModified() {
        String mapKey = getMapKey();
        NacosServerConfig config = getStringNacosConfigMap().get(mapKey);
        if (config == null) return false;
        return !serverField.getText().equals(config.getServerAddr()) ||
                !namespaceField.getText().equals(config.getNamespace()) ||
                !usernameField.getText().equals(config.getUsername()) ||
                !exportPathField.getText().equals(NacosGlobalConfigState.getInstance().getExportPath());
    }

    @Override
    public void apply() {
        String mapKey = getMapKey();
        NacosServerConfig config = getStringNacosConfigMap().computeIfAbsent(mapKey, k -> new NacosServerConfig());
        config.setServerAddr(serverField.getText());
        config.setNamespace(namespaceField.getText());
        config.setUsername(usernameField.getText());
        SecurePasswordStorage.savePassword(currentEnvironment, new String(passwordField.getPassword()));
        config.setEnv(currentEnvironment);

        // Save export path to global config
        String exportPath = exportPathField.getText();
        NacosGlobalConfigState.getInstance().setExportPath(exportPath);

        Messages.showMessageDialog("Save successful!", "Success", Messages.getInformationIcon());
    }

    private void exportConfiguration() {
        String exportPath = NacosGlobalConfigState.getInstance().getExportPath();
        if (StringUtils.isBlank(exportPath)) {
            Messages.showErrorDialog("Please provide a valid export path!", "Export Error");
            return;
        }

        // Export logic using exportPath (you can implement your actual export logic here)
        Messages.showMessageDialog("Configuration will be exported to: " + exportPath, "Export Successful", Messages.getInformationIcon());
    }

    @Override
    public String getDisplayName() {
        return "Nacos Plugin Global Configuration";
    }
}
