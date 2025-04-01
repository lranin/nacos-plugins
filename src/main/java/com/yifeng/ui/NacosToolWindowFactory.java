package com.yifeng.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.yifeng.client.NacosClient;
import com.yifeng.model.NacosConfig;
import com.yifeng.service.NacosConfigState;
import com.yifeng.utils.NacosConfigFileUtil;
import com.yifeng.utils.StringDiffUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NacosToolWindowFactory implements ToolWindowFactory, DumbAware {
    private final NacosClient nacosClient = new NacosClient();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建主面板
        JBPanel<?> panel = new JBPanel<>();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // 添加一个标签
        panel.add(new JBLabel("Nacos 配置"));

        // 创建并添加环境选择下拉框
        String[] environments = {"dev", "test", "prod"};
        JComboBox<String> environmentComboBox = new ComboBox<>(environments);
        environmentComboBox.setPreferredSize(new Dimension(100, 20)); // 调整下拉框大小
        panel.add(environmentComboBox);

        // 创建并添加目标环境选择下拉框
        JComboBox<String> targetEnvironmentComboBox = new ComboBox<>(environments);
        targetEnvironmentComboBox.setPreferredSize(new Dimension(100, 20)); // 调整下拉框大小
        panel.add(new JBLabel("目标环境"));
        panel.add(targetEnvironmentComboBox);

        // 新增拉取配置按钮
        JButton fetchConfigButton = new JButton("拉取配置");
        panel.add(fetchConfigButton);

        JButton compareButton = new JButton("对比配置");
        panel.add(compareButton);

        // 将面板添加到 Tool Window
        toolWindow.getComponent().add(panel);

        // 添加拉取配置按钮的点击事件
        fetchConfigButton.addActionListener(e -> {
            String selectedEnvironment = (String) environmentComboBox.getSelectedItem();
            String config = nacosClient.loadConfig(selectedEnvironment);

            // 以文件的形式展示
            NacosConfigFileUtil.showConfigInEditor(project, selectedEnvironment + "-config.yaml", config);
        });

        // 添加对比按钮的点击事件
        compareButton.addActionListener(e -> {
            String selectedEnvironment = (String) environmentComboBox.getSelectedItem();
            String targetEnvironment = (String) targetEnvironmentComboBox.getSelectedItem(); // 获取目标环境
            // 调用加载远程Nacos配置的方法
            String config1 = nacosClient.loadConfig(selectedEnvironment);
            String config2 = nacosClient.loadConfig(targetEnvironment); // 使用目标环境
            // 对比配置并显示结果
//                showCompareResult(config1, config2);
            StringDiffUtil.compareStrings(project, config1, selectedEnvironment, config2, targetEnvironment);
        });
    }

    // 新增方法：格式检查
    private String validateConfigFormat(String config) {
        // 这里添加实际的格式检查逻辑
        if (config == null || config.isEmpty()) {
            return "配置为空";
        }
        return "格式正确";
    }

    // 修改方法：显示对比结果
    private void showCompareResult(String config1, String config2) {
        // 创建一个新的窗口
        JFrame compareFrame = new JFrame("配置对比结果");
        compareFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        compareFrame.setSize(600, 400);

        // 创建一个面板用于显示对比结果
        JBPanel<?> panel = new JBPanel<>();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // 添加对比结果标签
        JBLabel resultLabel = new JBLabel("配置对比结果:");
        panel.add(resultLabel);

        // 添加对比结果文本区域
        JTextArea resultTextArea = new JTextArea(20, 50);
        resultTextArea.setText("配置1:\n" + config1 + "\n\n配置2:\n" + config2 + "\n\n格式检查结果:\n配置1: " + "todo" + "\n配置2: " + "todo");
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        panel.add(scrollPane);

        // 将面板添加到窗口
        compareFrame.add(panel);

        // 显示窗口
        compareFrame.setVisible(true);
    }
}