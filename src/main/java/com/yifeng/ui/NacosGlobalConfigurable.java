package com.yifeng.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.yifeng.model.EnvironmentEnums;
import com.yifeng.model.NacosServerConfig;
import com.yifeng.service.NacosGlobalConfigState;
import com.yifeng.utils.SecurePasswordStorage;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class NacosGlobalConfigurable implements Configurable {
    private JPanel mainPanel;
    private JComboBox<String> serverConfigDropdown;
    private JTextField serverField;
    private JTextField namespaceField;
    private JTextField usernameField;
    private JPasswordField passwordField;
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
        serverConfigDropdown = new ComboBox<>(new String[]{"配置1", "配置2", "配置3"});
        serverConfigDropdown.addActionListener(e -> loadSettings());
        panel.add(serverConfigDropdown);

        for (EnvironmentEnums env : EnvironmentEnums.values()) {
            JButton button = new JButton(env.name());
            button.addActionListener(e -> switchEnvironment(env));
            panel.add(button);
        }
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        serverField = new JTextField(20);
        namespaceField = new JTextField(20);
        usernameField = new JTextField(20);
        usernameField.setText("reader");
        passwordField = new JPasswordField(20);
        saveButton = new JButton("保存");

        panel.add(createLabeledField("服务器地址:", serverField), gbc);
        gbc.gridy++;
        panel.add(createLabeledField("用户名:", usernameField), gbc);
        gbc.gridy++;
        panel.add(createLabeledField("Namespace:", namespaceField), gbc);
        gbc.gridy++;
        panel.add(createLabeledField("密码:", passwordField), gbc);
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(saveButton, gbc);

        saveButton.addActionListener(e -> apply());
        return panel;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(new JLabel(labelText), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void switchEnvironment(EnvironmentEnums environment) {
        currentEnvironment = environment.getEnv();
        loadSettings();
    }

    private void loadSettings() {
        String mapKey = getMapKey();
        NacosServerConfig config = getStringNacosConfigMap().getOrDefault(mapKey, new NacosServerConfig());
        String serverAddr = config.getServerAddr();
        String username = config.getUsername();
        String namespace = config.getNamespace();
        serverField.setText(StringUtils.isBlank(serverAddr) ? "http://139.9.50.212:8848" : serverAddr);
        if (StringUtils.isBlank(namespace)) {
            if (currentEnvironment.equals(EnvironmentEnums.DEV.getEnv())) {
                namespace = "develop";
            }
            if (currentEnvironment.equals(EnvironmentEnums.TEST.getEnv())) {
                namespace = "testing";
            }
            if (currentEnvironment.equals(EnvironmentEnums.PROD.getEnv())) {
                namespace = "product";
            }
        }
        namespaceField.setText(StringUtils.isBlank(namespace) ? "public" : namespace);
        usernameField.setText(StringUtils.isBlank(username) ? "reader" : username);
    }

    private @NotNull String getMapKey() {
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
                !usernameField.getText().equals(config.getUsername());
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
    }

    @Override
    public String getDisplayName() {
        return "Nacos插件环境配置";
    }
}
