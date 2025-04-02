package com.yifeng.model;

/**
 * @author : liuruiming
 * @since : 2025/04/01 10:07
 * desc: TODO
 */
public enum EnvironmentEnums {
    DEV("dev"),
    TEST("test"),
    PROD("prod");

    private String env;

    EnvironmentEnums(String env) {
        this.env = env;
    }

    public String getEnv() {
        return env;
    }
}
