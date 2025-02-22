package com.baiyi.opscloud.workorder.entry;

import com.baiyi.opscloud.common.util.JSONUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author baiyi
 * @Date 2022/7/18 13:56
 * @Version 1.0
 */
public class ApplicationScaleReplicasEntry {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KubernetesDeployment implements Serializable {

        private static final long serialVersionUID = -2057324997536563597L;

        @ApiModelProperty(value = "数据实例UUID")
        private String instanceUuid;

        @ApiModelProperty(value = "namespace:deployment")
        private String name;

        private String namespace;

        private String deploymentName;

        // 原始值
        private Integer replicas;

        // 扩容后的
        private Integer scaleReplicas;

        private Integer id;

        private String comment;

        @Override
        public String toString() {
            return JSONUtil.writeValueAsString(this);
        }
    }

}
