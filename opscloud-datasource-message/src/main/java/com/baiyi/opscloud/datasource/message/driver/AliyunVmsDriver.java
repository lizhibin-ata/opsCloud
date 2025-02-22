package com.baiyi.opscloud.datasource.message.driver;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.baiyi.opscloud.common.datasource.AliyunConfig;
import com.baiyi.opscloud.common.util.JSONUtil;
import com.baiyi.opscloud.datasource.aliyun.core.SimpleAliyunClient;
import com.baiyi.opscloud.datasource.message.AliyunVmsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @Author 修远
 * @Date 2022/7/22 10:47 PM
 * @Since 1.0
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunVmsDriver {

    private final SimpleAliyunClient aliyunClient;

    private static final String CALL_SHOW_NUM = "057156140102";
    private static final String CALL_PROD_ID = "11000000300006";
    private static final String OK = "OK";
    private static final String CALL_OK = "用户接听";

    // calld可以通过QueryCallDetailByCallId接口查询呼叫详情。
    public String singleCallByTts(String regionId, AliyunConfig.Aliyun aliyun, String phone, String ttsCode) {
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dyvmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SingleCallByTts");
        request.putQueryParameter("CalledShowNumber", CALL_SHOW_NUM);
        request.putQueryParameter("CalledNumber", phone);
        request.putQueryParameter("TtsCode", ttsCode);
        IAcsClient iAcsClient = aliyunClient.buildAcsClient(regionId, aliyun);
        try {
            CommonResponse response = iAcsClient.getCommonResponse(request);
            if (response.getHttpStatus() == 200) {
                AliyunVmsResponse.SingleCallByTts data =
                        JSONUtil.readValue(response.getData(), AliyunVmsResponse.SingleCallByTts.class);
                if (OK.equals(data.getCode()))
                    return data.getCallId();
                log.error("singleCallByTts失败: err={}", data.getMessage());
            }
        } catch (ClientException e) {
            log.error("singleCallByTts失败: err={}", e.getMessage());
        }
        return StringUtils.EMPTY;
    }

    public Boolean queryCallDetailByCallId(String regionId, AliyunConfig.Aliyun aliyun, String callId, Long queryDate) {
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dyvmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("QueryCallDetailByCallId");
        request.putQueryParameter("ProdId", CALL_PROD_ID);
        request.putQueryParameter("CallId", callId);
        request.putQueryParameter("QueryDate", String.valueOf(queryDate));
        IAcsClient iAcsClient = aliyunClient.buildAcsClient(regionId, aliyun);
        try {
            CommonResponse response = iAcsClient.getCommonResponse(request);
            if (response.getHttpStatus() == 200) {
                AliyunVmsResponse.QueryCallDetailByCallId data =
                        JSONUtil.readValue(response.getData(), AliyunVmsResponse.QueryCallDetailByCallId.class);
                if (OK.equals(data.getCode())) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(data.getData());
                    return CALL_OK.equals(jsonNode.get("stateDesc").asText());
                }
                log.error("queryCallDetailByCallId失败: err={}", data.getMessage());
                return false;
            }
        } catch (ClientException e) {
            log.error("queryCallDetailByCallId失败: err={}", e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("queryCallDetailByCallId失败: err={}", e.getMessage());
        }
        return false;
    }


}
