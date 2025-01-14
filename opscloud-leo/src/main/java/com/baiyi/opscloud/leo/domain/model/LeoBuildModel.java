package com.baiyi.opscloud.leo.domain.model;

import com.baiyi.opscloud.domain.generator.opscloud.LeoBuild;
import com.baiyi.opscloud.leo.domain.model.base.YamlDump;
import com.baiyi.opscloud.leo.exception.LeoJobException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.List;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2022/11/8 16:23
 * @Version 1.0
 */
@Slf4j
public class LeoBuildModel {

    public static BuildConfig load(LeoBuild leoBuild) {
        return load(leoBuild.getBuildConfig());
    }

    /**
     * 从配置加载
     *
     * @param config
     * @return
     */
    public static BuildConfig load(String config) {
        if (StringUtils.isEmpty(config))
            return BuildConfig.EMPTY_BUILD;
        try {
            Representer representer = new Representer(new DumperOptions());
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yaml = new Yaml(new Constructor(BuildConfig.class), representer);
            return yaml.loadAs(config, BuildConfig.class);
        } catch (Exception e) {
            throw new LeoJobException("转换配置文件错误: err={}", e.getMessage());
        }
    }

    @Builder
    @Data
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BuildConfig extends YamlDump {
        private static final BuildConfig EMPTY_BUILD = BuildConfig.builder().build();
        private Build build;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Build {
        private LeoBaseModel.GitLab gitLab;
        private LeoBaseModel.Jenkins jenkins;
        private LeoBaseModel.Notify notify;
        private String comment;
        // 字典
        private Map<String, String> dict;
        // 构建参数
        private List<LeoBaseModel.Parameter> parameters;
        // 构建标签
        private List<String> tags;
    }

}
