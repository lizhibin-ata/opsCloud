package com.baiyi.opscloud.leo.action.deploy.strategy.deploy;

import com.baiyi.opscloud.common.datasource.KubernetesConfig;
import com.baiyi.opscloud.datasource.kubernetes.driver.KubernetesDeploymentDriver;
import com.baiyi.opscloud.domain.generator.opscloud.LeoDeploy;
import com.baiyi.opscloud.leo.action.deploy.strategy.deploy.base.DoDeployStrategy;
import com.baiyi.opscloud.domain.constants.DeployTypeConstants;
import com.baiyi.opscloud.leo.domain.model.LeoBaseModel;
import com.baiyi.opscloud.leo.domain.model.LeoDeployModel;
import com.baiyi.opscloud.leo.exception.LeoDeployException;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * @Author baiyi
 * @Date 2022/12/14 10:57
 * @Version 1.0
 */
@Slf4j
@Component
public class DoDeployWithRedeployStrategy extends DoDeployStrategy {

    /**
     * 执行部署
     * @param leoDeploy
     * @param deployConfig
     * @param kubernetesConfig
     * @param deployment
     */
    @Override
    protected void doDeploy(LeoDeploy leoDeploy, LeoDeployModel.DeployConfig deployConfig, KubernetesConfig kubernetesConfig, Deployment deployment){
        LeoBaseModel.Kubernetes kubernetes = Optional.ofNullable(deployConfig)
                .map(LeoDeployModel.DeployConfig::getDeploy)
                .map(LeoDeployModel.Deploy::getKubernetes)
                .orElseThrow(() -> new LeoDeployException("Kubernetes配置不存在！"));

        try {
            KubernetesDeploymentDriver.redeployDeployment(kubernetesConfig.getKubernetes(), deployment);
            LeoDeploy saveLeoDeploy = LeoDeploy.builder()
                    .id(leoDeploy.getId())
                    .startTime(new Date())
                    .deployConfig(deployConfig.dump())
                    .deployStatus("更新Deployment阶段: 成功")
                    .build();
            save(saveLeoDeploy, "更新Deployment成功");
        } catch (Exception e) {
            throw new LeoDeployException(e.getMessage());
        }
    }

    @Override
    public String getDeployType() {
        return DeployTypeConstants.REDEPLOY.name();
    }

}