package com.yifeng.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

public class NacosConfigFileUtil {

    public static void showConfigInEditor(@NotNull Project project, @NotNull String title, @NotNull String content) {
        ApplicationManager.getApplication().invokeLater(() -> {
            // 1. 查找是否已经有同名的文件
            VirtualFile existingFile = findOpenedFile(project, title);
            if (existingFile != null) {
                // 如果已经打开，就切换过去
                new OpenFileDescriptor(project, existingFile).navigate(true);
                return;
            }

            // 2. 没打开，新建临时文件
            LightVirtualFile virtualFile = new LightVirtualFile(title + ".yaml", content);

            // 3. 打开新文件
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
        });
    }

    private static VirtualFile findOpenedFile(Project project, String title) {
        VirtualFile[] openFiles = FileEditorManager.getInstance(project).getOpenFiles();
        for (VirtualFile file : openFiles) {
            if (file.getName().equals(title + ".yaml")) {
                return file;
            }
        }
        return null;
    }
}
