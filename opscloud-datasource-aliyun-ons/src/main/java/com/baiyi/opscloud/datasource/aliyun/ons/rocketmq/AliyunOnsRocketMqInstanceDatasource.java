package com.baiyi.opscloud.datasource.aliyun.ons.rocketmq;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.ons.model.v20190214.OnsInstanceBaseInfoRequest;
import com.aliyuncs.ons.model.v20190214.OnsInstanceBaseInfoResponse;
import com.aliyuncs.ons.model.v20190214.OnsInstanceInServiceListRequest;
import com.aliyuncs.ons.model.v20190214.OnsInstanceInServiceListResponse;
import com.baiyi.opscloud.common.datasource.AliyunConfig;
import com.baiyi.opscloud.datasource.aliyun.core.AliyunClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/9/30 2:08 下午
 * @Version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunOnsRocketMqInstanceDatasource {

    private final AliyunClient aliyunClient;

    public List<OnsInstanceInServiceListResponse.InstanceVO> listInstance(String regionId, AliyunConfig.Aliyun aliyun) {
        OnsInstanceInServiceListRequest request = new OnsInstanceInServiceListRequest();
        try {
            OnsInstanceInServiceListResponse response = aliyunClient.getAcsResponse(regionId, aliyun, request);
            return response == null ? Collections.emptyList() : response.getData();
        } catch (ClientException e) {
            log.error("查询ONS实例列表失败", e);
            return Collections.emptyList();
        }
    }

    public OnsInstanceBaseInfoResponse.InstanceBaseInfo getInstanceInfo(String regionId, AliyunConfig.Aliyun aliyun, String instanceId) {
        OnsInstanceBaseInfoRequest request = new OnsInstanceBaseInfoRequest();
        request.setInstanceId(instanceId);
        try {
            OnsInstanceBaseInfoResponse response = aliyunClient.getAcsResponse(regionId, aliyun, request);
            return response.getInstanceBaseInfo();
        } catch (ClientException e) {
            log.error("查询ONS实例详情失败", e);
            return null;
        }
    }
}