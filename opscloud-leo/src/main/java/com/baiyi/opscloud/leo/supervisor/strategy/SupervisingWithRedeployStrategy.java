package com.baiyi.opscloud.leo.supervisor.strategy;

import com.baiyi.opscloud.common.datasource.KubernetesConfig;
import com.baiyi.opscloud.datasource.kubernetes.driver.KubernetesPodDriver;
import com.baiyi.opscloud.domain.generator.opscloud.LeoDeploy;
import com.baiyi.opscloud.domain.vo.leo.LeoDeployingVO;
import com.baiyi.opscloud.domain.constants.DeployTypeConstants;
import com.baiyi.opscloud.leo.domain.model.LeoBaseModel;
import com.baiyi.opscloud.leo.domain.model.LeoDeployModel;
import com.baiyi.opscloud.leo.exception.LeoDeployException;
import com.baiyi.opscloud.leo.supervisor.strategy.base.SupervisingStrategy;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.baiyi.opscloud.domain.vo.leo.LeoDeployingVO.MAX_RESTART;

/**
 * @Author baiyi
 * @Date 2022/12/13 16:53
 * @Version 1.0
 */
@Slf4j
@Component
public class SupervisingWithRedeployStrategy extends SupervisingStrategy {

    @Override
    @Retryable(value = LeoDeployException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000, multiplier = 1.5))
    protected LeoDeployingVO.Deploying getDeploying(LeoDeploy leoDeploy,
                                                    LeoDeployModel.DeployConfig deployConfig,
                                                    KubernetesConfig.Kubernetes kubernetes,
                                                    LeoDeployModel.Deploy deploy,
                                                    LeoBaseModel.Deployment deployment) {
        List<Pod> pods = KubernetesPodDriver.listPod(kubernetes, deployment.getNamespace(), deployment.getName());
        if (CollectionUtils.isEmpty(pods)) {
            return LeoDeployingVO.Deploying.builder().build();
        }
        final String containerName = deployment.getContainer().getName();
        final String image = deploy.getDeployVersion1().getImage();

        LeoDeployingVO.VerionDetails beforeRedeployVersion = LeoDeployingVO.VerionDetails.builder()
                .title("重启前")
                .image(image)
                .build();

        LeoDeployingVO.VerionDetails afterRedeployVersion = LeoDeployingVO.VerionDetails.builder()
                .title("重启后")
                .image(image)
                .build();

        List<LeoDeployingVO.PodDetails> podDetailsList = Optional.ofNullable(deployConfig)
                .map(LeoDeployModel.DeployConfig::getDeploy)
                .map(LeoDeployModel.Deploy::getDeployVersion1)
                .map(LeoDeployModel.DeployVersion::getPods)
                .orElseThrow(() -> new LeoDeployException("原Pod信息不存在"));

        /**
         * Map<String podIP, LeoDeployingVO.PodDetails podDetails>
         */
        Map<String, LeoDeployingVO.PodDetails> originalPodMap = podDetailsList
                .stream()
                .collect(Collectors.toMap(LeoDeployingVO.PodDetails::getPodIP, a -> a, (k1, k2) -> k1));
        pods.forEach(pod -> {
            LeoDeployingVO.PodDetails podDetails = podDetailsHelper.toPodDetails(pod, containerName);
            if (originalPodMap.containsKey(pod.getStatus().getPodIP())) {
                // 重启前
                beforeRedeployVersion.putPod(podDetails);
            } else {
                // 重启后
                afterRedeployVersion.putPod(podDetails);
            }
        });

        return LeoDeployingVO.Deploying.builder()
                .deployType(deploy.getDeployType())
                .versionDetails1(beforeRedeployVersion)
                .versionDetails2(afterRedeployVersion)
                .replicas(deployment.getReplicas())
                .build()
                .init();
    }

    @Override
    protected boolean verifyFinish(LeoDeploy leoDeploy, LeoDeployingVO.Deploying deploying) {
        // redeploy结束
        if (deploying.getIsFinish() && CollectionUtils.isEmpty(deploying.getVersionDetails1().getPods())) {
            LeoDeploy saveLeoDeploy = LeoDeploy.builder()
                    .id(leoDeploy.getId())
                    .endTime(new Date())
                    .deployResult("SUCCESS")
                    .deployStatus("执行部署任务阶段: 结束")
                    .isFinish(true)
                    .isActive(true)
                    .build();
            leoDeployService.updateByPrimaryKeySelective(saveLeoDeploy);
            return true;
        }
        return false;
    }

    @Override
    protected void verifyError(LeoDeploy leoDeploy, LeoDeployingVO.Deploying deploying) {
        if (deploying.isMaxRestartError()) {
            LeoDeploy saveLeoDeploy = LeoDeploy.builder()
                    .id(leoDeploy.getId())
                    .endTime(new Date())
                    .deployResult("ERROR")
                    .deployStatus(String.format("执行部署任务阶段: 容器重启次数超过最大值 maxRestart=%s", MAX_RESTART))
                    .isFinish(true)
                    .isActive(false)
                    .build();
            leoDeployService.updateByPrimaryKeySelective(saveLeoDeploy);
        }
    }

    @Override
    public String getDeployType() {
        return DeployTypeConstants.REDEPLOY.name();
    }

}