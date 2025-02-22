package com.baiyi.opscloud.common.datasource;

import com.baiyi.opscloud.common.datasource.base.BaseDsConfig;
import com.google.common.base.Joiner;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * @Author baiyi
 * @Date 2021/6/17 3:54 下午
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AliyunConfig extends BaseDsConfig {

    // signin.aliyun.com
    //  private static final String RAM_LOGIN_URL = "https://signin.${VERSION}.com/${COMPANY}.onaliyun.com/login.htm";

    private static final String RAM_LOGIN_URL = "https://signin.%s.com/%s.onaliyun.com/login.htm";

    public static final String DMS_ENDPOINT = "dms-enterprise.aliyuncs.com";

    private Aliyun aliyun;

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Aliyun {

        private String version;
        private Account account;
        private String regionId;
        private Set<String> regionIds; // 可用区
        private Ons ons;
        // 数据库管理
        private Dms dms;
        // 云监控
        private Cms cms;

        private Acr arc;

    }

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Account {

        private String uid;
        private String name;
        private String company;
        private String accessKeyId;
        private String secret;

        public String getDomain() {
            return Joiner.on("").skipNulls().join("@", StringUtils.isEmpty(this.company) ? this.uid : this.company, ".onaliyun.com");
        }

        /**
         * RAM登录地址
         *
         * @return
         */
        public String getLoginUrl(String version) {
            String aliyunVersion = StringUtils.isEmpty(version) ? "aliyun" : version;
            String aliyunCompany = StringUtils.isEmpty(this.company) ? this.uid : this.company;
            return String.format(RAM_LOGIN_URL, aliyunVersion, aliyunCompany);
        }

    }

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Ons {
        private String internetRegionId;
    }

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Dms {
        private String endpoint;
        private Long tid;
    }

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Cms {
        private Dingtalk dingtalk;
    }

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Dingtalk {
        private String token;
    }

    @Data
    @NoArgsConstructor
    @ApiModel
    public static class Acr {

        private List<Instance> instances;

        @Data
        @NoArgsConstructor
        @ApiModel
        public static class Instance {
            private String id;
            private String domain;
            private String desc;
        }

    }

}
