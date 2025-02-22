package com.baiyi.opscloud.common.datasource;

import com.baiyi.opscloud.common.datasource.base.BaseDsConfig;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @Author baiyi
 * @Date 2022/7/7 14:20
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HuaweicloudConfig extends BaseDsConfig {

    private Huaweicloud huaweicloud;

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Huaweicloud {

        private String version;
        private Account account;
        private String regionId;
        private Set<String> regionIds; // 可用区

    }

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Account {

        private String uid;
        private String username;
        private String accessKeyId;
        private String secretAccessKey;

    }

}
