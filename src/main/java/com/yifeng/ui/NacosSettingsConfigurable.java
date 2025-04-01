package com.yifeng.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.util.ui.JBUI;
import com.yifeng.model.NacosConfig;
import com.yifeng.service.NacosConfigState;
import com.yifeng.utils.SecurePasswordStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class NacosSettingsConfigurable implements Configurable {
    private JPanel mainPanel;

    private JTextField serverField;
    private JTextField namespaceField;
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField dataIdField;
    private JTextField groupField;
    public JButton saveButton = new JButton("保存");

    private String currentEnvironment = NacosConfig.DEV; // 默认环境为开发环境

    private void switchEnvironment(String environment) {
        currentEnvironment = environment;
        loadSettings();
    }

    @Override
    public @Nullable JComponent createComponent() {
        mainPanel = new JPanel(new GridLayout(4, 2, 10, 0)); // 4行2列，水平间隙10，垂直间隙5
        // 创建顶部环境切换标签
        JPanel topPanel = topPanel();

        // 创建表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // 初始化字段
        serverField = new JTextField(20); // 设置合适的列数（如20）
        namespaceField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JTextField(20);
        dataIdField = new JTextField(20);
        groupField = new JTextField(20);

        // 创建标签和文本框的容器
        JPanel serverPanel = createLabeledField("服务器地址:", serverField);
        JPanel namespacePanel = createLabeledField("namespace:", namespaceField);
        JPanel usernamePanel = createLabeledField("用户名:   ", usernameField);
        JPanel passwordPanel = createLabeledField("密码:   ", passwordField);
        JPanel appIdPanel = createLabeledField("appId:     ", dataIdField);
        JPanel groupPanel = createLabeledField("group:     ", groupField);

        // 添加到表单面板
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(serverPanel, gbc);
        gbc.gridy = 1;
        formPanel.add(usernamePanel, gbc);
//        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(namespacePanel, gbc);
        gbc.gridy = 3;
        formPanel.add(passwordPanel, gbc);
        gbc.gridy = 4;
        formPanel.add(appIdPanel, gbc);
        gbc.gridy = 5;
        formPanel.add(groupPanel, gbc);

        // 添加保存按钮
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(saveButton, gbc);

        // 添加保存按钮的事件监听器
        saveButton.addActionListener(e -> apply());

        // 将顶部面板和表单面板添加到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        loadSettings();
        return mainPanel;
    }

    private @NotNull JPanel topPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton devButton = new JButton("开发");
        JButton testButton = new JButton("测试");
        JButton prodButton = new JButton("生产");
        topPanel.add(devButton);
        topPanel.add(testButton);
        topPanel.add(prodButton);

        // 添加环境切换按钮的事件监听器
        devButton.addActionListener(e -> switchEnvironment(NacosConfig.DEV));
        prodButton.addActionListener(e -> switchEnvironment(NacosConfig.PROD));
        testButton.addActionListener(e -> switchEnvironment(NacosConfig.TEST));
        return topPanel;
    }

    // 新增方法：创建带标签的容器
    private JPanel createLabeledField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0)); // 水平间距5，垂直0
        JLabel jLabel = new JLabel(labelText);

        // 设置标签和文本框为等宽字体
        Font monoFont = new Font("Monospaced", Font.PLAIN, 14);
        jLabel.setFont(monoFont);
        field.setFont(monoFont);

        panel.add(jLabel, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void loadSettings() {
        Map<String, NacosConfig> configMap = getStringNacosConfigMap();
        NacosConfig nacosConfig = configMap.get(currentEnvironment);
        if (nacosConfig != null) {
            serverField.setText(nacosConfig.getServerAddr());
            namespaceField.setText(nacosConfig.getNamespace());
            usernameField.setText(nacosConfig.getUsername()); // 假设存在 getUsername 方法
            dataIdField.setText(nacosConfig.getDataId());
            groupField.setText(nacosConfig.getGroup());
        } else {
            serverField.setText("");
            namespaceField.setText("");
            usernameField.setText("");
            dataIdField.setText("");
            groupField.setText("");
        }
    }

    private Map<String, NacosConfig> getStringNacosConfigMap() {
        NacosConfigState state = NacosConfigState.getInstance();
        return state.getConfigMap();
    }

    @Override
    public boolean isModified() {
        Map<String, NacosConfig> configMap = getStringNacosConfigMap();
        NacosConfig nacosConfig = configMap.get(currentEnvironment);
        if (nacosConfig == null) {
            return false;
        }
        return !serverField.getText().equals(nacosConfig.getServerAddr()) ||
                !namespaceField.getText().equals(nacosConfig.getNamespace()) ||
                !usernameField.getText().equals(nacosConfig.getUsername());
    }

    @Override
    public void apply() {
        Map<String, NacosConfig> configMap = getStringNacosConfigMap();
        NacosConfig nacosConfig = configMap.get(currentEnvironment);
        if (nacosConfig == null) {
            nacosConfig = new NacosConfig();
        }
        nacosConfig.setServerAddr(serverField.getText());
        nacosConfig.setNamespace(namespaceField.getText());
        nacosConfig.setUsername(usernameField.getText()); // 假设存在 setUsername 方法
        SecurePasswordStorage.savePassword(currentEnvironment, passwordField.getText());
//        nacosConfig.setPasswordKey(currentEnvironment); // 假设存在 setPassword 方法
        nacosConfig.setEnv(currentEnvironment);
        nacosConfig.setGroup(groupField.getText());
        nacosConfig.setDataId(dataIdField.getText());
        configMap.put(currentEnvironment, nacosConfig);
    }

    @Override
    public String getDisplayName() {
        return "Nacos插件配置";
    }
}