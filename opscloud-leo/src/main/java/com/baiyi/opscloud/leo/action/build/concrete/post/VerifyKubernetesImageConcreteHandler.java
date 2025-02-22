package com.baiyi.opscloud.leo.action.build.concrete.post;

import com.baiyi.opscloud.core.InstanceHelper;
import com.baiyi.opscloud.core.factory.DsConfigHelper;
import com.baiyi.opscloud.datasource.aliyun.acr.driver.AliyunAcrImageDriver;
import com.baiyi.opscloud.datasource.aliyun.acr.driver.AliyunAcrRepositoryDriver;
import com.baiyi.opscloud.domain.generator.opscloud.LeoBuild;
import com.baiyi.opscloud.domain.generator.opscloud.LeoBuildImage;
import com.baiyi.opscloud.domain.generator.opscloud.LeoJob;
import com.baiyi.opscloud.leo.action.build.BaseBuildHandler;
import com.baiyi.opscloud.leo.action.build.concrete.post.validator.base.BaseCrValidator;
import com.baiyi.opscloud.leo.action.build.concrete.post.validator.factory.CrValidatorFactory;
import com.baiyi.opscloud.leo.constants.BuildDictConstants;
import com.baiyi.opscloud.leo.domain.model.LeoBuildModel;
import com.baiyi.opscloud.leo.domain.model.LeoJobModel;
import com.baiyi.opscloud.leo.exception.LeoBuildException;
import com.baiyi.opscloud.service.leo.LeoBuildImageService;
import com.baiyi.opscloud.service.leo.LeoJobService;
import com.baiyi.opscloud.service.sys.EnvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * @Author baiyi
 * @Date 2022/11/17 16:11
 * @Version 1.0
 */
@Slf4j
@Component
public class VerifyKubernetesImageConcreteHandler extends BaseBuildHandler {

    @Resource
    private LeoJobService leoJobService;

    @Resource
    private EnvService envService;

    @Resource
    private InstanceHelper instanceHelper;

    @Resource
    private DsConfigHelper dsConfigHelper;

    @Resource
    private AliyunAcrRepositoryDriver aliyunAcrRepositoryDriver;

    @Resource
    private AliyunAcrImageDriver aliyunAcrImageDriver;

    @Resource
    private LeoBuildImageService leoBuildImageService;

    private static final String KUBERNETES_IMAGE = "kubernetes-image";

    /**
     * 验证 Kubernetes Image
     *
     * @param leoBuild
     * @param buildConfig
     */
    @Override
    protected void handle(LeoBuild leoBuild, LeoBuildModel.BuildConfig buildConfig) {
        LeoJob leoJob = leoJobService.getById(leoBuild.getJobId());
        LeoJobModel.JobConfig jobConfig = LeoJobModel.load(leoJob.getJobConfig());
        final String buildType = Optional.ofNullable(jobConfig)
                .map(LeoJobModel.JobConfig::getJob)
                .map(LeoJobModel.Job::getBuild)
                .map(LeoJobModel.Build::getType)
                .orElse(KUBERNETES_IMAGE);
        if (!KUBERNETES_IMAGE.equalsIgnoreCase(buildType)) return;

        final LeoJobModel.CR cr = Optional.ofNullable(jobConfig)
                .map(LeoJobModel.JobConfig::getJob)
                .map(LeoJobModel.Job::getCr)
                .orElseThrow(() -> new LeoBuildException("任务CR配置不存在无法验证镜像是否推送成功！"));

        final String crType = Optional.of(cr)
                .map(LeoJobModel.CR::getType)
                .orElseThrow(() -> new LeoBuildException("任务CR类型配置不存在无法验证镜像是否推送成功！"));

        BaseCrValidator crValidator = CrValidatorFactory.getValidatorByCrType(crType);
        if (crValidator == null) {
            throw new LeoBuildException("任务CR类型配置不正确: crType={}", crType);
        }
        try {
            // 校验
            crValidator.verifyImage(leoJob, leoBuild, cr, buildConfig);
            // 记录构建镜像
            Map<String, String> dict = buildConfig.getBuild().getDict();
            LeoBuildImage leoBuildImage = LeoBuildImage.builder()
                    .buildId(leoBuild.getId())
                    .jobId(leoJob.getId())
                    .image(dict.get(BuildDictConstants.IMAGE.getKey()))
                    .versionName(leoBuild.getVersionName())
                    .versionDesc(leoBuild.getVersionDesc())
                    .isActive(true)
                    .build();
            leoBuildImageService.add(leoBuildImage);
            // 设置构建任务有效
            LeoBuild saveLeoBuild = LeoBuild.builder()
                    .id(leoBuild.getId())
                    .isActive(true)
                    .build();
            leoBuildService.updateByPrimaryKeySelective(saveLeoBuild);
        } catch (LeoBuildException e) {
            logHelper.error(leoBuild, e.getMessage());
        }
    }

}
