package com.yifeng.client;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.yifeng.model.EnvironmentEnums;
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
                throw new RuntimeException("创建Nacos配置服务失败: " + e.getMessage());
            }
        }
        try {
            return configService.getConfig(dataId, group, 5000);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

}