package com.yifeng.client;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.intellij.openapi.ui.Messages;
import com.yifeng.model.NacosServerConfig;
import com.yifeng.service.NacosGlobalConfigState;
import com.yifeng.utils.SecurePasswordStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author : liuruiming
 * @since : 2025/03/31 17:00
 * desc:
 */
public class NacosClient {
    Map<String, ConfigService> configServiceMap = new HashMap<>();

    public String loadConfig(String nacosTag, String environment, String dataId, String group) {
        String mapKey = nacosTag + environment;
        NacosGlobalConfigState state = NacosGlobalConfigState.getInstance();
        NacosServerConfig nacosServerConfig = state.getConfigMap().get(mapKey);
        if (nacosServerConfig == null) {
            Messages.showErrorDialog(String.format("未找到环境配置,tag: %s, environment: %s", mapKey, environment), "Nacos配置服务连接失败");
            throw new RuntimeException("未找到环境配置: " + mapKey);
        }
        ConfigService configService = configServiceMap.get(mapKey);
        if (configService == null) {
            Properties properties = new Properties();
            properties.put("serverAddr", nacosServerConfig.getServerAddr());
            properties.put("username", nacosServerConfig.getUsername());
            properties.put("password", SecurePasswordStorage.loadPassword(nacosServerConfig.getEnv()));
            properties.put("namespace", nacosServerConfig.getNamespace());
            try {
                configService = NacosFactory.createConfigService(properties);
                configServiceMap.put(environment, configService);
            } catch (NacosException e) {
                Messages.showErrorDialog(String.format("连接Nacos配置服务器失败: %s", e.getMessage()), "Nacos配置服务连接失败");
                throw new RuntimeException(e);
            }
        }
        try {
            String config = configService.getConfig(dataId, group, 5000);
            if (config == null || config.isEmpty()) {
                Messages.showErrorDialog(String.format("未找到或无法拉取配置: %s@%s", dataId, group), "Nacos配置为空");
                throw new RuntimeException(String.format("未找到或无法拉取配置: %s@%s", dataId, group));
            }
            return config;
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

}