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
            fetchConfig(environmentComboBox, panel);
        });

        // 添加对比按钮的点击事件
        compareButton.addActionListener(e -> {
            compareConfig(project, environmentComboBox, targetEnvironmentComboBox, panel);
        });
    }

    private void compareConfig(@NotNull Project project, JComboBox<String> environmentComboBox, JComboBox<String> targetEnvironmentComboBox, JBPanel<?> panel) {
        String selectedEnvironment = (String) environmentComboBox.getSelectedItem();
        String targetEnvironment = (String) targetEnvironmentComboBox.getSelectedItem(); // 获取目标环境
        try {
            String selectedConfig = nacosClient.loadConfig(selectedEnvironment);
            String targetConfig = nacosClient.loadConfig(targetEnvironment); // 使用目标环境
            // 对比配置并显示结果
            StringDiffUtil.compareStrings(project, selectedConfig, selectedEnvironment, targetConfig, targetEnvironment);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(panel, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchConfig(JComboBox<String> environmentComboBox, JBPanel<?> panel) {
        String selectedEnvironment = (String) environmentComboBox.getSelectedItem();
        try {
            String config = nacosClient.loadConfig(selectedEnvironment);
            // 弹窗提醒
            JOptionPane.showMessageDialog(panel, config, selectedEnvironment + "-config.yaml", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(panel, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 新增方法：格式检查
    private String validateConfigFormat(String config) {
        // 这里添加实际的格式检查逻辑
        if (config == null || config.isEmpty()) {
            return "配置为空";
        }
        return "格式正确";
    }
}