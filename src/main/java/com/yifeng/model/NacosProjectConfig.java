package com.yifeng.model;

/**
 * @author : liuruiming
 * @since : 2025/04/01 14:15
 * desc: 项目级别的配置
 */
public class NacosProjectConfig {
    private String dataId;
    private String group;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
