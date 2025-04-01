package com.yifeng.utils;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.project.Project;

public class StringDiffUtil {
    public static void compareStrings(Project project, String text1, String title1, String text2, String title2) {
        // 创建 DiffContent
        DiffContent content1 = DiffContentFactory.getInstance().create(text1);
        DiffContent content2 = DiffContentFactory.getInstance().create(text2);

        // 创建 Diff 请求
        SimpleDiffRequest diffRequest = new SimpleDiffRequest(
                "文本对比", // 标题
                content1, content2,
                title1, title2
        );

        // 调用对比窗口
        DiffManager.getInstance().showDiff(project, diffRequest);
    }
}
