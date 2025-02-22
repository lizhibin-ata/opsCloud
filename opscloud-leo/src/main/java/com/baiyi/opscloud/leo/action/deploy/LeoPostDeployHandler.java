package com.baiyi.opscloud.leo.action.deploy;

import com.baiyi.opscloud.common.config.ThreadPoolTaskConfiguration;
import com.baiyi.opscloud.domain.generator.opscloud.LeoDeploy;
import com.baiyi.opscloud.leo.action.deploy.concrete.post.EndDeployNotificationConcreteHandler;
import com.baiyi.opscloud.leo.domain.model.LeoDeployModel;
import com.baiyi.opscloud.service.leo.LeoDeployService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2022/12/8 12:51
 * @Version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeoPostDeployHandler implements InitializingBean {

    protected final LeoDeployService leoDeployService;

    // 结束部署通知
    @Resource
    private EndDeployNotificationConcreteHandler endDeployNotificationConcreteHandler;

    @Async(value = ThreadPoolTaskConfiguration.TaskPools.CORE)
    public void handleDeploy(LeoDeploy leoDeploy, LeoDeployModel.DeployConfig deployConfig) {
        // 使用责任链设计模式解耦代码
        endDeployNotificationConcreteHandler.handleRequest(leoDeployService.getById(leoDeploy.getId()), deployConfig);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

}
