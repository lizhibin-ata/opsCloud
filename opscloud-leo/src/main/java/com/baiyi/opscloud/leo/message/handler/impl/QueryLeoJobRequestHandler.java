package com.baiyi.opscloud.leo.message.handler.impl;

import com.baiyi.opscloud.common.util.BeanCopierUtil;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.LeoJob;
import com.baiyi.opscloud.domain.param.leo.request.QueryLeoJobLeoRequestParam;
import com.baiyi.opscloud.domain.param.leo.request.type.LeoRequestType;
import com.baiyi.opscloud.domain.vo.leo.LeoJobVO;
import com.baiyi.opscloud.leo.message.handler.base.BaseLeoContinuousDeliveryRequestHandler;
import com.baiyi.opscloud.leo.message.response.LeoContinuousDeliveryResponse;
import com.baiyi.opscloud.leo.packer.LeoJobResponsePacker;
import com.baiyi.opscloud.service.leo.LeoJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.Session;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2022/11/23 16:10
 * @Version 1.0
 */
@Slf4j
@Component
public class QueryLeoJobRequestHandler extends BaseLeoContinuousDeliveryRequestHandler<QueryLeoJobLeoRequestParam> {

    @Resource
    private LeoJobService leoJobService;

    @Resource
    private LeoJobResponsePacker leoJobResponsePacker;

    @Override
    public String getMessageType() {
        return LeoRequestType.QUERY_LEO_JOB.name();
    }

    @Override
    public void handleRequest(Session session, String message) {
        QueryLeoJobLeoRequestParam param = toRequestParam(message);
        DataTable<LeoJobVO.Job> dataTable = queryLeoJobPage(param);
        LeoContinuousDeliveryResponse response = LeoContinuousDeliveryResponse.builder()
                .body(dataTable)
                .messageType(getMessageType())
                .build();
        sendToSession(session, response);
    }

    public DataTable<LeoJobVO.Job> queryLeoJobPage(QueryLeoJobLeoRequestParam pageQuery) {
        DataTable<LeoJob> table = leoJobService.queryJobPage(pageQuery);
        List<LeoJobVO.Job> data = BeanCopierUtil.copyListProperties(table.getData(), LeoJobVO.Job.class).stream()
                .peek(leoJobResponsePacker::wrap)
                .collect(Collectors.toList());
        return new DataTable<>(data, table.getTotalNum());
    }

    private QueryLeoJobLeoRequestParam toRequestParam(String message) {
        return toLeoRequestParam(message, QueryLeoJobLeoRequestParam.class);
    }

}
