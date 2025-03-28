package com.yifeng.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ShowPopupAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Messages.showMessageDialog(
            "这是一个测试弹窗！",
            "测试提示",
            Messages.getInformationIcon()
        );
    }
}
