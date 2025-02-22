package com.baiyi.opscloud.schedule.task;

import com.baiyi.opscloud.common.annotation.TaskWatch;
import com.baiyi.opscloud.config.condition.EnvCondition;
import com.baiyi.opscloud.domain.annotation.InstanceHealth;
import com.baiyi.opscloud.leo.task.LeoDeployCompensationTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author baiyi
 * @Date 2022/12/26 14:26
 * @Version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
// 非生产环境不执行
@Conditional(EnvCondition.class)
public class LeoDeployTask {

    private final LeoDeployCompensationTask leoDeployCompensationTask;

    private static final boolean enable = false;

    @InstanceHealth // 实例健康检查，高优先级
    @Scheduled(initialDelay = 15000, fixedRate = 120 * 1000)
    @TaskWatch(name = "Leo deploy compensate")
    public void run() {
        leoDeployCompensationTask.handleTask();
    }

}

