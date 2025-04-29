package com.yifeng.service;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.yifeng.model.NacosProjectConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author : liuruiming
 * @since : 2025/04/01 14:12
 * desc: 持久化
 */
@Service(Service.Level.PROJECT)
@State(
        name = "NacosProjectConfig",
        storages = @Storage(StoragePathMacros.WORKSPACE_FILE) // 存储到项目级别
)
public final class NacosProjectConfigState implements PersistentStateComponent<NacosProjectConfig> {
    NacosProjectConfig nacosProjectConfig = new NacosProjectConfig();

    public static NacosProjectConfigState getInstance(Project project) {
        return project.getService(NacosProjectConfigState.class);
    }

    @Override
    public @NotNull NacosProjectConfig getState() {
        return this.nacosProjectConfig;
    }

    @Override
    public void loadState(@NotNull NacosProjectConfig state) {
        this.nacosProjectConfig = state;
    }

    public void setDataId(String dataId) {
        if (nacosProjectConfig == null) {
            nacosProjectConfig = new NacosProjectConfig();
        }
        nacosProjectConfig.setDataId(dataId);
    }

    public void setGroup(String group) {
        if (nacosProjectConfig == null) {
            nacosProjectConfig = new NacosProjectConfig();
        }
        nacosProjectConfig.setGroup(group);
    }

    public String getDataId() {
        return nacosProjectConfig != null ? nacosProjectConfig.getDataId() : null;
    }

    public String getGroup() {
        return nacosProjectConfig != null ? nacosProjectConfig.getGroup() : null;
    }
}