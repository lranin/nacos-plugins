package com.yifeng.utils;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class NacosConfigFileUtil {

    /**
     * 在 IntelliJ IDEA 中展示拉取的 Nacos 配置
     *
     * @param project  当前项目
     * @param fileName 文件名
     * @param content  配置内容
     */
    public static void showConfigInEditor(@NotNull Project project, @NotNull String fileName, @NotNull String content) {
        // 创建一个临时的 VirtualFile
        LightVirtualFile virtualFile = new LightVirtualFile(fileName, content);
        virtualFile.setCharset(StandardCharsets.UTF_8); // 设置字符编码

        // 打开文件
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openTextEditor(new OpenFileDescriptor(project, virtualFile), true);
    }
}
