package com.yifeng.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.yifeng.client.NacosClient;
import com.yifeng.model.EnvironmentEnums;
import com.yifeng.model.NacosProjectConfig;
import com.yifeng.model.NacosServerConfig;
import com.yifeng.service.NacosProjectConfigState;
import com.yifeng.utils.NacosConfigFileUtil;
import com.yifeng.utils.StringDiffUtil;
import com.yifeng.utils.YamlUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Objects;

public class NacosProjectConfigFactory implements ToolWindowFactory, DumbAware {
    private final NacosClient nacosClient = new NacosClient();
    private JBPanel<?> mainPanel;
    private JComboBox<String> serverConfigDropdown;
    private JTextField dataIdField;
    private JTextField groupField;
    JComboBox<String> environmentComboBox;
    JComboBox<String> targetEnvironmentComboBox;
    JButton fetchButton;
    JButton compareButton;
    JButton yamlCheckButton;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建并设置面板
        mainPanel = createMainPanel();

        // 添加组件到Tool Window
        toolWindow.getComponent().add(mainPanel);

        // 加载配置
        loadSettings();

        // 获取环境配置
        String selectedEnvironment = getSelectedEnvironment();
        String targetEnvironment = getTargetEnvironment();

        // 绑定按钮事件
        bindButtonActions(project, selectedEnvironment, targetEnvironment, mainPanel);
    }

    private @NotNull JBPanel<?> createMainPanel() {
        JBPanel<?> mainPanel = new JBPanel<>();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(JBUI.Borders.empty(10));

        // 添加标题标签
        mainPanel.add(new JBLabel("Nacos 配置"));

        // 添加顶部服务器配置下拉框
        mainPanel.add(createTopPanel());

        // 添加配置表单
        mainPanel.add(createFormPanel());

        // 添加环境选择下拉框
        mainPanel.add(createEnvironmentComboBox());

        // 添加目标环境选择下拉框
        mainPanel.add(createTargetEnvironmentComboBox());

        // 按钮面板（水平布局）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.add(fetchButton = new JButton("拉取配置"));
        buttonPanel.add(compareButton = new JButton("对比配置"));
        buttonPanel.add(yamlCheckButton = new JButton("YAML格式校验"));

        // 设置按钮大小
        Dimension buttonSize = new Dimension(120, 30);
        fetchButton.setPreferredSize(buttonSize);
        compareButton.setPreferredSize(buttonSize);
        yamlCheckButton.setPreferredSize(buttonSize);

        mainPanel.add(buttonPanel);

        return mainPanel;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        serverConfigDropdown = new ComboBox<>(new String[]{"配置1", "配置2", "配置3"});
        panel.add(serverConfigDropdown);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        dataIdField = new JTextField(20);
        gbc.gridy++;
        groupField = new JTextField(20);

        panel.add(createLabeledField("dataId:", dataIdField), gbc);
        gbc.gridy++;
        panel.add(createLabeledField("group:", groupField), gbc);
        return panel;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(new JLabel(labelText), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JComboBox<String> createEnvironmentComboBox() {
        String[] environments = Arrays.stream(EnvironmentEnums.values()).map(EnvironmentEnums::getEnv).toArray(String[]::new);
        environmentComboBox = new ComboBox<>(environments);
        environmentComboBox.setPreferredSize(new Dimension(100, 30));
        environmentComboBox.setMaximumSize(new Dimension(100, 30));
        return environmentComboBox;
    }

    private JComboBox<String> createTargetEnvironmentComboBox() {
        String[] environments = Arrays.stream(EnvironmentEnums.values()).map(EnvironmentEnums::getEnv).toArray(String[]::new);
        targetEnvironmentComboBox = new ComboBox<>(environments);
        targetEnvironmentComboBox.setPreferredSize(new Dimension(100, 20));
        targetEnvironmentComboBox.setMaximumSize(new Dimension(100, 20));
        return targetEnvironmentComboBox;
    }

    private void bindButtonActions(Project project, String selectedEnvironment, String targetEnvironment, JPanel mainPanel) {
        // 拉取配置按钮的点击事件
        fetchButton.addActionListener(e -> {
            try {
                fetchConfig(project);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(mainPanel, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 对比按钮的点击事件
        compareButton.addActionListener(e -> {
            try {
                compareConfig(project);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(mainPanel, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // YAML格式校验按钮
        yamlCheckButton.addActionListener(e -> {
            String selectedConfig = nacosClient.loadConfig(serverConfigDropdown.getSelectedItem().toString(), getSelectedEnvironment(), dataIdField.getText(), groupField.getText());
            String validYaml = YamlUtils.isValidYaml(selectedConfig);
            if (StringUtils.isNotBlank(validYaml)) {
                JOptionPane.showMessageDialog(mainPanel, validYaml, "YAML格式错误", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPanel, "YAML格式正确", "YAML格式正确", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void fetchConfig(Project project) {
        try {
            saveProjectConfig();
            String config = nacosClient.loadConfig(serverConfigDropdown.getSelectedItem().toString(), getSelectedEnvironment(), dataIdField.getText(), groupField.getText());
            NacosConfigFileUtil.showConfigInEditor(project, getSelectedEnvironment() + "-配置文件", config);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveProjectConfig() {
        NacosProjectConfig projectConfig = getProjectConfig();
        projectConfig.setDataId(dataIdField.getText());
        projectConfig.setGroup(groupField.getText());
    }

    private void compareConfig(Project project) {
        try {
            saveProjectConfig();
            String selectedConfig = nacosClient.loadConfig(serverConfigDropdown.getSelectedItem().toString(), getSelectedEnvironment(), dataIdField.getText(), groupField.getText());
            String targetConfig = nacosClient.loadConfig(serverConfigDropdown.getSelectedItem().toString(), getTargetEnvironment(), dataIdField.getText(), groupField.getText());
            StringDiffUtil.compareStrings(project, selectedConfig, getSelectedEnvironment(), targetConfig, getTargetEnvironment());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private @NotNull NacosProjectConfig getProjectConfig() {
        return NacosProjectConfigState.getInstance().getState();
    }

    private String getSelectedEnvironment() {
        return Objects.requireNonNull(environmentComboBox.getSelectedItem()).toString();
    }

    private String getTargetEnvironment() {
        return Objects.requireNonNull(targetEnvironmentComboBox.getSelectedItem()).toString();
    }

    private void loadSettings() {
        NacosProjectConfig projectConfig = getProjectConfig();
        dataIdField.setText(projectConfig.getDataId() != null ? projectConfig.getDataId() : "");
        groupField.setText(projectConfig.getGroup() != null ? projectConfig.getGroup() : "");
    }
}
