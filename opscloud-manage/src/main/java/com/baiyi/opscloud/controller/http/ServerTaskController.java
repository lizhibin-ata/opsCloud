package com.baiyi.opscloud.controller.http;

import com.baiyi.opscloud.common.HttpResult;
import com.baiyi.opscloud.common.util.SessionUtil;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.param.task.ServerTaskParam;
import com.baiyi.opscloud.domain.vo.task.ServerTaskVO;
import com.baiyi.opscloud.facade.task.ServerTaskFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @Author baiyi
 * @Date 2021/9/18 3:40 下午
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/server/task")
@Api(tags = "服务器任务管理")
@RequiredArgsConstructor
public class ServerTaskController {

    private final ServerTaskFacade serverTaskFacade;

    @ApiOperation(value = "分页查询服务器任务列表")
    @PostMapping(value = "/page/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult<DataTable<ServerTaskVO.ServerTask>> queryServerTaskPage(@RequestBody @Valid ServerTaskParam.ServerTaskPageQuery pageQuery) {
        return new HttpResult<>(serverTaskFacade.queryServerTaskPage(pageQuery));
    }

    @ApiOperation(value = "提交服务器任务")
    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult<Boolean> submitServerTask(@RequestBody @Valid ServerTaskParam.SubmitServerTask submitServerTask) {
        serverTaskFacade.submitServerTask(submitServerTask, SessionUtil.getUsername());
        return HttpResult.SUCCESS;
    }

}
