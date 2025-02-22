package com.baiyi.opscloud.workorder.processor.impl;

import com.baiyi.opscloud.common.constants.enums.DsTypeEnum;
import com.baiyi.opscloud.common.datasource.KubernetesConfig;
import com.baiyi.opscloud.datasource.kubernetes.driver.KubernetesDeploymentDriver;
import com.baiyi.opscloud.datasource.kubernetes.exception.KubernetesDeploymentException;
import com.baiyi.opscloud.domain.generator.opscloud.WorkOrderTicket;
import com.baiyi.opscloud.domain.generator.opscloud.WorkOrderTicketEntry;
import com.baiyi.opscloud.domain.param.workorder.WorkOrderTicketEntryParam;
import com.baiyi.opscloud.workorder.constants.OrderTicketPhaseCodeConstants;
import com.baiyi.opscloud.workorder.constants.WorkOrderKeyConstants;
import com.baiyi.opscloud.workorder.entry.ApplicationScaleReplicasEntry;
import com.baiyi.opscloud.workorder.exception.TicketProcessException;
import com.baiyi.opscloud.workorder.exception.TicketVerifyException;
import com.baiyi.opscloud.workorder.processor.impl.extended.AbstractDsAssetExtendedBaseTicketProcessor;
import com.baiyi.opscloud.workorder.query.impl.ApplicationScaleReplicasEntryQuery;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * @Author baiyi
 * @Date 2022/7/18 11:40
 * @Version 1.0
 */
@Slf4j
@Component
public class ApplicationScaleReplicasTicketProcessor extends AbstractDsAssetExtendedBaseTicketProcessor<ApplicationScaleReplicasEntry.KubernetesDeployment, KubernetesConfig> {

    private final String SCALE_REPLICAS = "scaleReplicas";

    @Override
    protected void processHandle(WorkOrderTicketEntry ticketEntry, ApplicationScaleReplicasEntry.KubernetesDeployment entry) throws TicketProcessException {
        KubernetesConfig.Kubernetes config = getDsConfig(ticketEntry, KubernetesConfig.class).getKubernetes();
        try {
            KubernetesDeploymentDriver.scaleDeploymentReplicas(config, entry.getNamespace(), entry.getDeploymentName(), entry.getScaleReplicas());
            log.info("工单扩容应用副本: instanceUuid={}, entry={}", ticketEntry.getInstanceUuid(), entry);
        } catch (KubernetesDeploymentException e) {
            throw new TicketProcessException("工单扩容应用副本失败: {}", e.getMessage());
        }
    }

    @Override
    public void update(WorkOrderTicketEntryParam.TicketEntry ticketEntry) {
        WorkOrderTicket ticket = ticketService.getById(ticketEntry.getWorkOrderTicketId());
        if (!OrderTicketPhaseCodeConstants.NEW.name().equals(ticket.getTicketPhase()))
            throw new TicketProcessException("工单进度不是新建，无法更新配置条目！");
        WorkOrderTicketEntry preTicketEntry = ticketEntryService.getById(ticketEntry.getId());
        Map<String, String> properties = ticketEntry.getProperties();
        if (properties == null) return;
        if (!properties.containsKey(SCALE_REPLICAS)) return;
        ApplicationScaleReplicasEntry.KubernetesDeployment deployment = toEntry(preTicketEntry.getContent());
        deployment.setScaleReplicas(Integer.parseInt(properties.get(SCALE_REPLICAS)));
        preTicketEntry.setContent(deployment.toString());
        preTicketEntry.setComment(ApplicationScaleReplicasEntryQuery.getComment(deployment));
        updateTicketEntry(preTicketEntry);
    }

    @Override
    public void verifyHandle(WorkOrderTicketEntryParam.TicketEntry ticketEntry) throws TicketVerifyException {
        ApplicationScaleReplicasEntry.KubernetesDeployment entry = this.toEntry(ticketEntry.getContent());
        KubernetesConfig.Kubernetes config = getDsConfig(ticketEntry, KubernetesConfig.class).getKubernetes();
        if (StringUtils.isEmpty(entry.getNamespace()))
            throw new TicketVerifyException("校验工单条目失败: 未指定Namespace命名空间！");
        if (StringUtils.isEmpty(entry.getDeploymentName()))
            throw new TicketVerifyException("校验工单条目失败: 未指定Deployment(无状态)名称！");
        if (entry.getScaleReplicas() == null)
            throw new TicketVerifyException("校验工单条目失败: 未指定扩容后副本数！");

        int replicas = Optional.ofNullable(KubernetesDeploymentDriver.getDeployment(config, entry.getNamespace(), entry.getDeploymentName()))
                .map(Deployment::getSpec)
                .map(DeploymentSpec::getReplicas)
                .orElseThrow(() -> new TicketVerifyException("校验工单条目失败: 无法获取当前副本数！"));
        if (entry.getScaleReplicas() <= replicas)
            throw new TicketVerifyException("校验工单条目失败: 扩容后副本数小于当前副本数！");
    }

    @Override
    public String getKey() {
        return WorkOrderKeyConstants.APPLICATION_SCALE_REPLICAS.name();
    }

    @Override
    public String getInstanceType() {
        return DsTypeEnum.KUBERNETES.name();
    }

    @Override
    protected Class<ApplicationScaleReplicasEntry.KubernetesDeployment> getEntryClassT() {
        return ApplicationScaleReplicasEntry.KubernetesDeployment.class;
    }

    @Override
    protected void process(WorkOrderTicketEntry ticketEntry, ApplicationScaleReplicasEntry.KubernetesDeployment entry) throws TicketProcessException {
        processHandle(ticketEntry, entry);
        KubernetesConfig.Kubernetes config = getDsConfig(ticketEntry, KubernetesConfig.class).getKubernetes();
        try {
            Deployment deployment = KubernetesDeploymentDriver.getDeployment(config, entry.getNamespace(), entry.getDeploymentName());
            dsInstanceFacade.pullAsset(ticketEntry.getInstanceUuid(), getAssetType(), deployment);
        } catch (Exception e) {
            throw new TicketProcessException("应用副本扩容失败: {}", e.getMessage());
        }
    }

}

