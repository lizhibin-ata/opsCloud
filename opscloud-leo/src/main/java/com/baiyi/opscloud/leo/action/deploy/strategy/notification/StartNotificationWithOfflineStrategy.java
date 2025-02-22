package com.baiyi.opscloud.leo.action.deploy.strategy.notification;

import com.baiyi.opscloud.domain.constants.DeployTypeConstants;
import com.baiyi.opscloud.domain.generator.opscloud.LeoDeploy;
import com.baiyi.opscloud.leo.action.deploy.strategy.notification.base.StartNotificationStrategy;
import com.baiyi.opscloud.leo.domain.model.LeoDeployModel;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author baiyi
 * @Date 2022/12/26 17:46
 * @Version 1.0
 */
@Component
public class StartNotificationWithOfflineStrategy extends StartNotificationStrategy {

    private static final String LEO_OFFLINE_START = "LEO_OFFLINE_START";

    @Override
    protected Map<String, Object> buildContentMap(LeoDeploy leoDeploy, LeoDeployModel.DeployConfig deployConfig) {
        return Maps.newHashMap();
    }

    @Override
    public String getDeployType() {
        return DeployTypeConstants.OFFLINE.name();
    }

    @Override
    protected String getMessageKey() {
        return LEO_OFFLINE_START;
    }

}
