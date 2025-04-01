package com.yifeng.client;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.yifeng.model.NacosConfig;
import com.yifeng.service.NacosConfigState;
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



    // 新增方法：拉取Nacos配置
    public String loadConfig(String environment) {
        NacosConfigState state = NacosConfigState.getInstance();
        NacosConfig nacosConfig = state.getConfigMap().get(environment);
        if (nacosConfig == null) {
            throw new RuntimeException("未找到环境配置: " + environment);
        }
        ConfigService configService = configServiceMap.get(environment);
        if (configService == null) {
            Properties properties = new Properties();
            properties.put("serverAddr", nacosConfig.getServerAddr());
            properties.put("username", nacosConfig.getUsername());
            properties.put("password", SecurePasswordStorage.loadPassword(nacosConfig.getEnv()));
            properties.put("namespace", nacosConfig.getNamespace());
            try {
                configService = NacosFactory.createConfigService(properties);
                configServiceMap.put(environment, configService);
            } catch (NacosException e) {
                throw new RuntimeException("创建Nacos配置服务失败: " + e.getMessage());
            }
        }
        try {
            return configService.getConfig(nacosConfig.getDataId(), nacosConfig.getGroup(), 5000);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}