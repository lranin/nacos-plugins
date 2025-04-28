package com.yifeng.model;

/**
 * @author : liuruiming
 * @since : 2025/04/27 11:30
 * desc:
 */
public enum ServerName {
    NORMAL("normal","普通nacos","http://139.9.50.212:8848"),
    CLUSTER("cluster","集群nacos","http://121.37.192.246:8848"),
    ELSE("else","其他nacos","")
    ;

    ServerName(String code, String serverName, String serverAddr) {
        this.code = code;
        this.serverName = serverName;
        this.serverAddr = serverAddr;
    }

    private String code;
    private String serverName;
    private String serverAddr;

    public String getCode() {
        return code;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerAddr() {
        return serverAddr;
    }
}
