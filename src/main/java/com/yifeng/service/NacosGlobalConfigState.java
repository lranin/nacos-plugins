package com.yifeng.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.yifeng.model.EnvironmentEnums;
import com.yifeng.model.NacosServerConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Service
@State(
        name = "NacosConfigState",
        storages = @Storage("NacosConfig.xml")
)
public final class NacosGlobalConfigState implements PersistentStateComponent<NacosGlobalConfigState> {
    public Map<String, NacosServerConfig> getConfigMap() {
        return configMap;
    }

    public Map<String, NacosServerConfig> configMap = new HashMap<>();

    public static NacosGlobalConfigState getInstance() {
        return com.intellij.openapi.components.ServiceManager.getService(NacosGlobalConfigState.class);
    }

    @Override
    public @NotNull NacosGlobalConfigState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull NacosGlobalConfigState state) {
        if (state.configMap != null && !state.configMap.isEmpty()) {
            this.configMap.putAll(state.configMap);
        }
    }
}
