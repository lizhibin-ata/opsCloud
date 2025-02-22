package com.baiyi.opscloud.leo.action.build.concrete.pre;

import com.baiyi.opscloud.common.datasource.JenkinsConfig;
import com.baiyi.opscloud.datasource.jenkins.driver.JenkinsJobDriver;
import com.baiyi.opscloud.domain.generator.opscloud.LeoBuild;
import com.baiyi.opscloud.domain.generator.opscloud.LeoJob;
import com.baiyi.opscloud.domain.generator.opscloud.User;
import com.baiyi.opscloud.leo.action.build.BaseBuildHandler;
import com.baiyi.opscloud.leo.action.build.helper.ApplicationTagsHelper;
import com.baiyi.opscloud.leo.constants.BuildDictConstants;
import com.baiyi.opscloud.leo.domain.model.LeoBaseModel;
import com.baiyi.opscloud.leo.domain.model.LeoBuildModel;
import com.baiyi.opscloud.leo.domain.model.LeoJobModel;
import com.baiyi.opscloud.leo.exception.LeoBuildException;
import com.baiyi.opscloud.service.application.ApplicationService;
import com.baiyi.opscloud.service.leo.LeoJobService;
import com.baiyi.opscloud.service.sys.EnvService;
import com.baiyi.opscloud.service.user.UserService;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2022/11/14 19:47
 * @Version 1.0
 */
@Slf4j
@Component
public class DoBuildConcreteHandler extends BaseBuildHandler {

    @Resource
    private LeoJobService leoJobService;

    @Resource
    private ApplicationService applicationService;

    @Resource
    private EnvService envService;

    @Resource
    private JenkinsJobDriver jenkinsJobDriver;

    @Resource
    private UserService userService;

    @Resource
    private ApplicationTagsHelper applicationTagsHelper;

    /**
     * 执行构建
     *
     * @param leoBuild
     * @param buildConfig
     */
    @Override
    protected void handle(LeoBuild leoBuild, LeoBuildModel.BuildConfig buildConfig) {
        LeoBaseModel.DsInstance dsInstance = buildConfig.getBuild().getJenkins().getInstance();
        JenkinsConfig jenkinsConfig = getJenkinsConfigWithUuid(dsInstance.getUuid());
        try {
            LeoJob leoJob = leoJobService.getById(leoBuild.getJobId());
            // 先构建字典，再构建参数
            Map<String, String> dict = generateBuildDict(leoBuild, buildConfig, leoJob);
            buildConfig.getBuild().setDict(dict);
            Map<String, String> params = generateBuildParamMap(leoBuild, buildConfig, leoJob);
            jenkinsJobDriver.buildJobWithParams(jenkinsConfig.getJenkins(), leoBuild.getBuildJobName(), params);
            LeoBuild saveLeoBuild = LeoBuild.builder()
                    .id(leoBuild.getId())
                    .buildStatus("执行构建任务阶段: 成功")
                    .buildConfig(buildConfig.dump())
                    // 设置启动时间
                    .startTime(new Date())
                    .build();
            save(saveLeoBuild, "执行构建任务成功: jenkinsName={}, jobName={}", dsInstance.getName(), leoBuild.getBuildJobName());
        } catch (URISyntaxException | IOException e) {
            LeoBuild saveLeoBuild = LeoBuild.builder()
                    .id(leoBuild.getId())
                    .endTime(new Date())
                    .isFinish(true)
                    .buildResult(RESULT_ERROR)
                    .buildStatus("执行构建任务阶段")
                    .build();
            save(saveLeoBuild);
            throw new LeoBuildException("执行构建任务错误: jenkinsName={}, uuid={}, jobName={}", dsInstance.getName(), dsInstance.getUuid(), leoBuild.getBuildJobName());
        }
    }

    /**
     * 生成构建参数
     *
     * @param leoBuild
     * @param buildConfig
     * @param leoJob
     * @return
     */
    private Map<String, String> generateBuildParamMap(LeoBuild leoBuild, LeoBuildModel.BuildConfig buildConfig, LeoJob leoJob) {
        List<LeoBaseModel.Parameter> jobParameters = Optional.of(buildConfig)
                .map(LeoBuildModel.BuildConfig::getBuild)
                .map(LeoBuildModel.Build::getParameters)
                .orElse(Lists.newArrayList());

        Map<String, String> dict = Optional.of(buildConfig)
                .map(LeoBuildModel.BuildConfig::getBuild)
                .map(LeoBuildModel.Build::getDict)
                .orElseThrow(() -> new LeoBuildException("执行构建任务错误, 构建字典不存在！"));

        Map<String, String> paramMap = jobParameters.stream()
                .collect(Collectors.toMap(LeoBaseModel.Parameter::getName, LeoBaseModel.Parameter::getValue, (k1, k2) -> k1));

        paramMap.put(BuildDictConstants.PROJECT.getKey(), dict.get(BuildDictConstants.PROJECT.getKey()));
        paramMap.put(BuildDictConstants.REGISTRY_URL.getKey(), dict.get(BuildDictConstants.REGISTRY_URL.getKey()));
        paramMap.put(BuildDictConstants.BRANCH.getKey(), dict.get(BuildDictConstants.BRANCH.getKey()));
        paramMap.put(BuildDictConstants.COMMIT_ID.getKey(), dict.get(BuildDictConstants.COMMIT_ID.getKey()));
        paramMap.put(BuildDictConstants.SSH_URL.getKey(), dict.get(BuildDictConstants.SSH_URL.getKey()));
        paramMap.put(BuildDictConstants.ENV.getKey(), dict.get(BuildDictConstants.ENV.getKey()));
        paramMap.put(BuildDictConstants.JOB_BUILD_NUMBER.getKey(), String.valueOf(leoBuild.getBuildNumber()));
        paramMap.put(BuildDictConstants.APPLICATION_NAME.getKey(), dict.get(BuildDictConstants.APPLICATION_NAME.getKey()));

        return paramMap;
    }

    /**
     * 生成构建字典
     *
     * @param leoBuild
     * @param buildConfig
     * @return
     */
    private Map<String, String> generateBuildDict(LeoBuild leoBuild, LeoBuildModel.BuildConfig buildConfig, LeoJob leoJob) {
        // 构建字典
        Map<String, String> dict = Optional.of(buildConfig)
                .map(LeoBuildModel.BuildConfig::getBuild)
                .map(LeoBuildModel.Build::getDict)
                .orElse(Maps.newHashMap());

        List<LeoBaseModel.Parameter> jobParameters = Optional.of(buildConfig)
                .map(LeoBuildModel.BuildConfig::getBuild)
                .map(LeoBuildModel.Build::getParameters)
                .orElse(Lists.newArrayList());
        Map<String, String> paramMap = jobParameters.stream()
                .collect(Collectors.toMap(LeoBaseModel.Parameter::getName, LeoBaseModel.Parameter::getValue, (k1, k2) -> k1));
        LeoJobModel.JobConfig jobConfig = LeoJobModel.load(leoJob);

        User user = userService.getByUsername(leoBuild.getUsername());
        final String displayName = Optional.ofNullable(user)
                .map(User::getDisplayName)
                .orElse(Optional.ofNullable(user)
                        .map(User::getName)
                        .orElse(leoBuild.getUsername())
                );

        final String applicationTags = applicationTagsHelper.getTagsStr(leoJob.getApplicationId());

        LeoBaseModel.GitLabProject gitLabProject = Optional.of(buildConfig)
                .map(LeoBuildModel.BuildConfig::getBuild)
                .map(LeoBuildModel.Build::getGitLab)
                .map(LeoBaseModel.GitLab::getProject)
                .orElseThrow(() -> new LeoBuildException("执行构建任务错误: 未指定GitLab项目配置！"));

        final String envName = getEnvNameWithLeoJob(leoJob);
        final String commit = gitLabProject.getCommit().getId();
        final String commitId = gitLabProject.getCommit().getId().substring(0, 8);
        final String buildNumber = String.valueOf(leoBuild.getBuildNumber());
        final String applicationName = applicationService.getById(leoJob.getApplicationId()).getApplicationKey();
        if (paramMap.containsKey(BuildDictConstants.REGISTRY_URL.getKey())) {
            dict.put(BuildDictConstants.REGISTRY_URL.getKey(), paramMap.get(BuildDictConstants.REGISTRY_URL.getKey()));
        } else {
            final String registryUrl = Optional.ofNullable(jobConfig)
                    .map(LeoJobModel.JobConfig::getJob)
                    .map(LeoJobModel.Job::getCr)
                    .map(LeoJobModel.CR::getInstance)
                    .map(LeoJobModel.CRInstance::getUrl)
                    .orElseThrow(() -> new LeoBuildException("执行构建任务错误: 未指定RegistryUrl配置！"));
            dict.put(BuildDictConstants.REGISTRY_URL.getKey(), registryUrl);
        }
        dict.put(BuildDictConstants.ENV.getKey(), envName);
        dict.put(BuildDictConstants.APPLICATION_NAME.getKey(), applicationName);
        dict.put(BuildDictConstants.APPLICATION_TAGS.getKey(), applicationTags);
        dict.put(BuildDictConstants.JOB_NAME.getKey(), leoBuild.getJobName());
        dict.put(BuildDictConstants.VERSION_NAME.getKey(), leoBuild.getVersionName());
        dict.put(BuildDictConstants.BUILD_NUMBER.getKey(), buildNumber);
        dict.put(BuildDictConstants.COMMIT.getKey(), commit);
        dict.put(BuildDictConstants.COMMIT_ID.getKey(), commitId);
        dict.put(BuildDictConstants.SSH_URL.getKey(), gitLabProject.getSshUrl());
        dict.put(BuildDictConstants.DISPLAY_NAME.getKey(), displayName);

        String project;
        if (paramMap.containsKey(BuildDictConstants.PROJECT.getKey())) {
            project = paramMap.get(BuildDictConstants.PROJECT.getKey());
        } else {
            project = Optional.ofNullable(jobConfig)
                    .map(LeoJobModel.JobConfig::getJob)
                    .map(LeoJobModel.Job::getCr)
                    .map(LeoJobModel.CR::getRepo)
                    .map(LeoJobModel.Repo::getName)
                    .orElseThrow(() -> new LeoBuildException("执行构建任务错误: 未指定Project配置！"));
        }
        dict.put(BuildDictConstants.PROJECT.getKey(), project);

        final String registryUrl = dict.get(BuildDictConstants.REGISTRY_URL.getKey());
        // example: 460e7585-19
        final String imageTag = Joiner.on("-").join(commitId, buildNumber);
        // example: aliyun-cr-uk.chuanyinet.com/daily/merchant-rss:460e7585-19
        dict.put(BuildDictConstants.IMAGE.getKey(), String.format("%s/%s/%s:%s", registryUrl, envName, project, imageTag));
        dict.put(BuildDictConstants.IMAGE_TAG.getKey(), imageTag);
        return dict;
    }

    private String getEnvNameWithLeoJob(final LeoJob leoJob) {
        return envService.getByEnvType(leoJob.getEnvType()).getEnvName();
    }

}
