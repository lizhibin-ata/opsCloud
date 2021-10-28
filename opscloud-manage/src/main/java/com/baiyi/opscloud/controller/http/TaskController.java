package com.baiyi.opscloud.controller.http;

import com.baiyi.opscloud.common.HttpResult;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.param.ansible.AnsiblePlaybookParam;
import com.baiyi.opscloud.domain.vo.ansible.AnsiblePlaybookVO;
import com.baiyi.opscloud.facade.task.AnsiblePlaybookFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author baiyi
 * @Date 2021/9/1 11:05 上午
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/task")
@Api(tags = "任务管理")
@RequiredArgsConstructor
public class TaskController {

    private final AnsiblePlaybookFacade ansiblePlaybookFacade;

    @ApiOperation(value = "分页查询剧本列表")
    @PostMapping(value = "/ansible/playbook/page/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult<DataTable<AnsiblePlaybookVO.Playbook>> queryAnsiblePlaybookPage(@RequestBody @Valid AnsiblePlaybookParam.AnsiblePlaybookPageQuery pageQuery) {
        return new HttpResult<>(ansiblePlaybookFacade.queryAnsiblePlaybookPage(pageQuery));
    }

    @ApiOperation(value = "新增剧本配置")
    @PostMapping(value = "/ansible/playbook/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult<Boolean> addAnsiblePlaybook(@RequestBody @Valid AnsiblePlaybookVO.Playbook playbook) {
        ansiblePlaybookFacade.addAnsiblePlaybook(playbook);
        return HttpResult.SUCCESS;
    }

    @ApiOperation(value = "更新剧本配置")
    @PutMapping(value = "/ansible/playbook/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult<Boolean> updateAnsiblePlaybook(@RequestBody @Valid AnsiblePlaybookVO.Playbook playbook) {
        ansiblePlaybookFacade.updateAnsiblePlaybook(playbook);
        return HttpResult.SUCCESS;
    }

    @ApiOperation(value = "删除指定的剧本配置")
    @DeleteMapping(value = "/ansible/playbook/del", produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult<Boolean> deleteServerById(@RequestParam int id) {
        ansiblePlaybookFacade.deleteAnsiblePlaybookById(id);
        return HttpResult.SUCCESS;
    }

}
