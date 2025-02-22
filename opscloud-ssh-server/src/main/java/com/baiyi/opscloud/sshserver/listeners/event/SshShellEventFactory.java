package com.baiyi.opscloud.sshserver.listeners.event;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author baiyi
 * @Date 2022/8/30 16:13
 * @Version 1.0
 */
@Slf4j
public class SshShellEventFactory {

    private static final Map<String, ISshShellEvent> context = new ConcurrentHashMap<>();

    public static ISshShellEvent getByType(String key) {
        return context.get(key);
    }

    public static void register(ISshShellEvent bean) {
        context.put(bean.getEventType(), bean);
        log.info("SshShellEventFactory Registered: evnetType={}, beanName={}", bean.getEventType(), bean.getClass().getSimpleName());
    }

}
