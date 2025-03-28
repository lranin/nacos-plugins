package com.yifeng.model;

public class NacosConfig {
    public static final String DEV = "dev";
    public static final String TEST = "test";
    public static final String PROD = "prod";

    private String serverAddr;
    private String namespace;
    private String env;

    public NacosConfig(String serverAddr, String namespace, String env) {
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.env = env;
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
}