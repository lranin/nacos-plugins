package com.yifeng.service;

import com.yifeng.model.NacosConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class NacosConfigManager {
    private final Map<String, NacosConfig> configMap = new HashMap<>();

    public void loadConfig(String configPath) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            properties.load(fis);
        }

        // Load development environment config
        configMap.put(NacosConfig.DEV, new NacosConfig(
            properties.getProperty("dev.serverAddr", ""),
            properties.getProperty("dev.namespace", ""),
            NacosConfig.DEV
        ));

        // Load testing environment config
        configMap.put(NacosConfig.TEST, new NacosConfig(
            properties.getProperty("test.serverAddr", ""),
            properties.getProperty("test.namespace", ""),
            NacosConfig.TEST
        ));

        // Load production environment config
        configMap.put(NacosConfig.PROD, new NacosConfig(
            properties.getProperty("prod.serverAddr", ""),
            properties.getProperty("prod.namespace", ""),
            NacosConfig.PROD
        ));
    }

    public NacosConfig getConfig(String env) {
        return configMap.get(env);
    }

    public Map<String, NacosConfig> getAllConfigs() {
        return Collections.unmodifiableMap(configMap);
    }

    public void clearConfigs() {
        configMap.clear();
    }
}