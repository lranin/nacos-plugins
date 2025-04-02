package com.yifeng.model;

public class NacosServerConfig {
    private String serverAddr;
    private String namespace;
    private String env;
    private String username;

    public NacosServerConfig() {
    }

    public NacosServerConfig(String serverAddr, String namespace, String username) {
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.username = username;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}