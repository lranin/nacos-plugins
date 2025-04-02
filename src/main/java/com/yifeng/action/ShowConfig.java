package com.yifeng.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.yifeng.model.NacosServerConfig;
import com.yifeng.service.NacosGlobalConfigState;

/**
 * @author : liuruiming
 * @since : 2025/03/31 10:45
 * desc: TODO
 */
public class ShowConfig extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        NacosGlobalConfigState state = NacosGlobalConfigState.getInstance();
        for (String server : state.configMap.keySet()) {
            NacosServerConfig config = state.configMap.get(server);
            System.out.println("服务器: " + config.getServerAddr() + ", Namespace: " + config.getNamespace());
        }
    }
}
