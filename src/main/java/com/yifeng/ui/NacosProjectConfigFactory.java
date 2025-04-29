package com.yifeng.ui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.yifeng.client.NacosClient;
import com.yifeng.model.EnvironmentEnums;
import com.yifeng.model.NacosProjectConfig;
import com.yifeng.model.ServerName;
import com.yifeng.service.NacosGlobalConfigState;
import com.yifeng.service.NacosProjectConfigState;
import com.yifeng.utils.NacosConfigFileUtil;
import com.yifeng.utils.PropertiesHighlighter;
import com.yifeng.utils.StringDiffUtil;
import com.yifeng.utils.YamlUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NacosProjectConfigFactory implements ToolWindowFactory, DumbAware {
    private final NacosClient nacosClient = new NacosClient();
    private JBPanel<?> mainPanel;
    private JComboBox<String> serverConfigDropdown;
    private JTextField dataIdField;
    private JTextField groupField;
    private JComboBox<String> environmentComboBox;
    private JComboBox<String> targetEnvironmentComboBox;
    private JButton fetchButton;
    private JButton compareButton;
    private JButton yamlCheckButton;
    private JButton propertiesCheckButton;
    private JButton exportButton;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        mainPanel = createMainPanel(project);
        toolWindow.getComponent().add(mainPanel);
        loadSettings(project);
    }

    private @NotNull JBPanel<?> createMainPanel(Project project) {
        JBPanel<?> panel = new JBPanel<>();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(15));

        panel.add(new JBLabel("Nacos 配置"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createServerConfigPanel());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createFormPanel());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createEnvironmentPanel());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createButtonPanel(project));

        return panel;
    }

    private JPanel createServerConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("服务器选择"));
        serverConfigDropdown = new ComboBox<>(Arrays.stream(ServerName.values()).map(ServerName::getServerName).toArray(String[]::new));
        serverConfigDropdown.setPreferredSize(new Dimension(300, 30));
        panel.add(serverConfigDropdown);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("配置参数"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        dataIdField = new JTextField();
        dataIdField.setPreferredSize(new Dimension(300, 30));
        groupField = new JTextField();
        groupField.setPreferredSize(new Dimension(300, 30));

        gbc.gridy++;
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

    private JPanel createEnvironmentPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("环境选择"));

        environmentComboBox = new ComboBox<>(Arrays.stream(EnvironmentEnums.values()).map(EnvironmentEnums::getEnv).toArray(String[]::new));
        targetEnvironmentComboBox = new ComboBox<>(Arrays.stream(EnvironmentEnums.values()).map(EnvironmentEnums::getEnv).toArray(String[]::new));
        environmentComboBox.setPreferredSize(new Dimension(140, 30));
        targetEnvironmentComboBox.setPreferredSize(new Dimension(140, 30));

        panel.add(new JLabel("源环境:"));
        panel.add(environmentComboBox);
        panel.add(new JLabel("目标环境:"));
        panel.add(targetEnvironmentComboBox);

        return panel;
    }

    private JPanel createButtonPanel(Project project) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("操作"));

        fetchButton = new JButton("拉取配置");
        compareButton = new JButton("对比配置");
        yamlCheckButton = new JButton("YAML格式校验");
        propertiesCheckButton = new JButton("prop格式校验");
        exportButton = new JButton("导出当前文件");

        Dimension buttonSize = new Dimension(160, 36); // 统一按钮大小
        fetchButton.setPreferredSize(buttonSize);
        compareButton.setPreferredSize(buttonSize);
        yamlCheckButton.setPreferredSize(buttonSize);
        propertiesCheckButton.setPreferredSize(buttonSize);
        exportButton.setPreferredSize(buttonSize);

        // 第一行：拉取 + 对比
        JPanel fetchComparePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        fetchComparePanel.add(fetchButton);
        fetchComparePanel.add(compareButton);

        // 第二行：YAML校验 + prop校验
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        checkPanel.add(yamlCheckButton);
        checkPanel.add(propertiesCheckButton);

        // 第三行：导出
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        exportPanel.add(exportButton);

        // 把三行按钮加到panel里
        panel.add(fetchComparePanel);
        panel.add(Box.createVerticalStrut(10)); // 每行之间留10px空隙
        panel.add(checkPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(exportPanel);

        bindButtonActions(project);
        return panel;
    }



    private void bindButtonActions(Project project) {
        fetchButton.addActionListener(e -> {
            runWithLoading(fetchButton, "拉取中...", () -> fetchConfig(project));
        });

        compareButton.addActionListener(e -> {
            runWithLoading(compareButton, "对比中...", () -> compareConfig(project));
        });

        yamlCheckButton.addActionListener(e -> {
            checkCurrentFileYaml(project);
        });

        exportButton.addActionListener(e -> {
            exportCurrentFile(project);
        });

        propertiesCheckButton.addActionListener(e -> {
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (editor == null) {
                Messages.showErrorDialog("未找到打开的文件", "错误");
                return;
            }

            String content = editor.getDocument().getText();

            boolean hasError = PropertiesHighlighter.highlightValidation(editor, content);

            if (hasError) {
                Messages.showErrorDialog("校验不通过", "提示");
            } else {
                Messages.showInfoMessage("校验通过", "提示");
            }
        });
    }

    private void fetchConfig(Project project) {
        try {
            saveProjectConfig(project);
            String config = nacosClient.loadConfig(serverConfigDropdown.getSelectedItem().toString(), getSelectedEnvironment(), dataIdField.getText(), groupField.getText());
            WriteCommandAction.runWriteCommandAction(project, () -> {
                NacosConfigFileUtil.showConfigInEditor(project, getSelectedEnvironment() + "-配置文件", config);
            });
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void compareConfig(Project project) {
        try {
            saveProjectConfig(project);
            String selectedConfig = nacosClient.loadConfig(serverConfigDropdown.getSelectedItem().toString(), getSelectedEnvironment(), dataIdField.getText(), groupField.getText());
            String targetConfig = nacosClient.loadConfig(serverConfigDropdown.getSelectedItem().toString(), getTargetEnvironment(), dataIdField.getText(), groupField.getText());
            StringDiffUtil.compareStrings(project, selectedConfig, getSelectedEnvironment(), targetConfig, getTargetEnvironment());
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void checkCurrentFileYaml(Project project) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            showError("未找到打开的文件");
            return;
        }
        String content = editor.getDocument().getText();
        String error = YamlUtils.isValidYaml(content);
        if (StringUtils.isNotBlank(error)) {
            showError(error);
        } else {
            Messages.showInfoMessage("YAML格式正确", "校验通过");
        }
    }

    private void exportCurrentFile(Project project) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            showError("未找到打开的文件");
            return;
        }

        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file == null) {
            showError("未找到文件");
            return;
        }

        // 获取全局配置中的导出路径
        String exportPath = NacosGlobalConfigState.getInstance().getExportPath();
        if (exportPath == null || exportPath.isEmpty()) {
            // 如果没有设置导出路径，则让用户选择一个路径
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(file.getName()));
            int option = fileChooser.showSaveDialog(mainPanel);
            if (option == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                    fos.write(editor.getDocument().getText().getBytes(StandardCharsets.UTF_8));
                    Messages.showInfoMessage("保存成功: " + saveFile.getAbsolutePath(), "导出成功");
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        } else {
            // 使用全局配置中的导出路径
            File saveFile = new File(exportPath, file.getName());
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                fos.write(editor.getDocument().getText().getBytes(StandardCharsets.UTF_8));
                Messages.showInfoMessage("保存成功: " + saveFile.getAbsolutePath(), "导出成功");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void saveProjectConfig(Project project) {
        NacosProjectConfig projectConfig = NacosProjectConfigState.getInstance(project).getState();
        projectConfig.setDataId(dataIdField.getText());
        projectConfig.setGroup(groupField.getText());
    }

    private String getSelectedEnvironment() {
        return Objects.requireNonNull(environmentComboBox.getSelectedItem()).toString();
    }

    private String getTargetEnvironment() {
        return Objects.requireNonNull(targetEnvironmentComboBox.getSelectedItem()).toString();
    }

    private void loadSettings(Project project) {
        NacosProjectConfig projectConfig = NacosProjectConfigState.getInstance(project).getState();
        dataIdField.setText(projectConfig.getDataId() != null ? projectConfig.getDataId() : "");
        groupField.setText(projectConfig.getGroup() != null ? projectConfig.getGroup() : "");
    }

    private void runWithLoading(JButton button, String loadingText, Runnable task) {
        button.setEnabled(false);
        String originalText = button.getText();
        button.setText(loadingText);
        SwingUtilities.invokeLater(() -> {
            try {
                task.run();
            } finally {
                button.setEnabled(true);
                button.setText(originalText);
            }
        });
    }

    private void showError(String message) {
        CopyPasteManager.getInstance().setContents(new StringSelection(message));
        Messages.showErrorDialog(mainPanel, message + "\n(已复制错误信息)", "错误");
    }
}
