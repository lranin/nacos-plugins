package com.yifeng.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class YamlUtils {

    // 校验字符串是否为有效的 YAML
    public static String isValidYaml(String yamlContent) {
        try {
            Yaml yaml = new Yaml();
            // 尝试解析 YAML 字符串
            yaml.load(yamlContent);
            return "";
        } catch (YAMLException e) {
            // 如果发生异常，说明不是有效的 YAML
            return e.getMessage();
        }
    }

    // 可选：打印格式化的 YAML
    public static String formatYaml(String yamlContent) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(yaml.load(yamlContent));
    }
}
