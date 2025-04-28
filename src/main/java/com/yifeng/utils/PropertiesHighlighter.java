package com.yifeng.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesHighlighter {

    public static boolean highlightValidation(Editor editor, String content) {
        boolean hasError = false;

        MarkupModel markupModel = editor.getMarkupModel();
        markupModel.removeAllHighlighters();

        String[] lines = content.split("\n");

        Pattern propertyPattern = Pattern.compile("^([^=\\s]+)\\s*=\\s*(.*)$");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            Matcher matcher = propertyPattern.matcher(line);
            if (!matcher.matches()) {
                // 格式不对
                markError(editor, i);
                hasError = true;
            } else {
                String value = matcher.group(2);
                if (value == null || value.trim().isEmpty()) {
                    // value为空
                    markError(editor, i);
                    hasError = true;
                }
            }
        }

        return hasError;
    }

    private static void markError(Editor editor, int lineIndex) {
        MarkupModel markupModel = editor.getMarkupModel();
        int startOffset = editor.getDocument().getLineStartOffset(lineIndex);
        int endOffset = editor.getDocument().getLineEndOffset(lineIndex);

        TextAttributes attributes = new TextAttributes();
        attributes.setForegroundColor(Color.RED);
        attributes.setEffectType(EffectType.WAVE_UNDERSCORE);
        attributes.setEffectColor(Color.RED);

        markupModel.addRangeHighlighter(startOffset, endOffset, HighlighterLayer.ERROR, attributes, HighlighterTargetArea.EXACT_RANGE);
    }
}
