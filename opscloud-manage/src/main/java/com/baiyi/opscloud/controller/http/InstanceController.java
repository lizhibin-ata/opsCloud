package com.baiyi.opscloud.controller.http;

import com.baiyi.opscloud.common.HttpResult;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.param.sys.RegisteredInstanceParam;
import com.baiyi.opscloud.domain.vo.env.EnvVO;
import com.baiyi.opscloud.domain.vo.sys.InstanceVO;
import com.baiyi.opscloud.facade.sys.InstanceFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Author baiyi
 * @Date 2021/9/3 2:12 下午
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/instance")
@Api(tags = "实例管理")
public class InstanceController {

    @Resource
    private InstanceFacade instanceFacade;


    @ApiOperation(value = "分页查询注册实例列表")
    @PostMapping(value = "/registered/page/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult<DataTable<InstanceVO.RegisteredInstance>> queryRegisteredInstancePage(@RequestBody @Valid RegisteredInstanceParam.RegisteredInstancePageQuery pageQuery) {
        return new HttpResult<>(instanceFacade.queryRegisteredInstancePage(pageQuery));
    }


    @ApiOperation(value = "负载均衡健康检查接口")
    @GetMapping(value = "/health/lb-check", produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult<InstanceVO.Health> checkHealth() {
        InstanceVO.Health health = instanceFacade.checkHealth();
        if (health.isHealth()) {
            return new HttpResult<>(health);
        } else {
            throw new ResourceInactiveException();
        }
    }

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public static class ResourceInactiveException extends RuntimeException {
        // 服务不可用 HTTP 503 Service Unavailable
    }
}