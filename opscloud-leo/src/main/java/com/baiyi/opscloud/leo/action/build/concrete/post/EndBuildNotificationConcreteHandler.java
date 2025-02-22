package com.baiyi.opscloud.leo.action.build.concrete.post;

import com.baiyi.opscloud.domain.generator.opscloud.LeoBuild;
import com.baiyi.opscloud.leo.action.build.BaseBuildHandler;
import com.baiyi.opscloud.leo.domain.model.LeoBuildModel;
import com.baiyi.opscloud.leo.exception.LeoBuildException;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author baiyi
 * @Date 2022/11/17 10:34
 * @Version 1.0
 */
@Slf4j
@Component
public class EndBuildNotificationConcreteHandler extends BaseBuildHandler {

    private static final String LEO_BUILD_END = "LEO_BUILD_END";

    /**
     * 构建结束通知
     *
     * @param leoBuild
     * @param buildConfig
     */
    @Override
    protected void handle(LeoBuild leoBuild, LeoBuildModel.BuildConfig buildConfig) {
        try {
            sendMessage(leoBuild, buildConfig);
            LeoBuild saveLeoBuild = LeoBuild.builder()
                    .id(leoBuild.getId())
                    .buildStatus("结束构建通知阶段: 发送消息成功")
                    .build();
            leoBuildService.updateByPrimaryKeySelective(saveLeoBuild);
            logHelper.info(leoBuild, "结束构建通知成功: jobName={}", leoBuild.getBuildJobName());
        } catch (LeoBuildException e) {
            LeoBuild saveLeoBuild = LeoBuild.builder()
                    .id(leoBuild.getId())
                    .buildStatus("结束构建通知阶段: 发送消息失败")
                    .build();
            leoBuildService.updateByPrimaryKeySelective(saveLeoBuild);
            // 忽略异常，只记录日志
            logHelper.warn(leoBuild, e.getMessage());
        }
    }

    private void sendMessage(LeoBuild leoBuild, LeoBuildModel.BuildConfig buildConfig) {
        Map<String, Object> contentMap = Maps.newHashMap();
        contentMap.put("buildPhase", "构建结束");
        contentMap.put("buildResult", leoBuild.getBuildResult());
        sendMessage(leoBuild, buildConfig, LEO_BUILD_END, contentMap);
    }

}
