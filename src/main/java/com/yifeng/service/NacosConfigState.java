package com.yifeng.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.yifeng.model.NacosConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Service
@State(
        name = "NacosConfigState",
        storages = @Storage("NacosConfig.xml")
)
public final class NacosConfigState implements PersistentStateComponent<NacosConfigState> {
    public Map<String, NacosConfig> getConfigMap() {
        return configMap;
    }

    public Map<String, NacosConfig> configMap = new HashMap<>();

    public static NacosConfigState getInstance() {
        return com.intellij.openapi.components.ServiceManager.getService(NacosConfigState.class);
    }

    @Override
    public @NotNull NacosConfigState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull NacosConfigState state) {
        this.configMap.clear();
        if (state.configMap != null) {
            this.configMap.putAll(state.configMap);
        }else {
            this.configMap.put(com.yifeng.model.NacosConfig.DEV, new NacosConfig("127.0.0.1:8848", ""));
        }
    }
}
