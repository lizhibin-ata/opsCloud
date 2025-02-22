package com.baiyi.opscloud.facade.leo;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.param.leo.LeoBuildParam;
import com.baiyi.opscloud.domain.param.leo.LeoJobParam;
import com.baiyi.opscloud.domain.vo.leo.LeoBuildVO;

/**
 * @Author baiyi
 * @Date 2022/11/8 16:15
 * @Version 1.0
 */
public interface LeoBuildFacade {

    /**
     * 手动执行构建
     *
     * @param doBuild
     */
    void doBuild(LeoBuildParam.DoBuild doBuild);


    /**
     * 停止构建
     *
     * @param buildId
     */
    void stopBuild(int buildId);

    /**
     * 构建页面查询项目分支
     *
     * @param getOptions
     * @return
     */
    LeoBuildVO.BranchOptions getBuildBranchOptions(LeoBuildParam.GetBuildBranchOptions getOptions);

    LeoBuildVO.BranchOptions createBuildBranch(LeoBuildParam.CreateBuildBranch createBuildBranch);

    void updateLeoBuild(LeoBuildParam.UpdateBuild updateBuild);

    DataTable<LeoBuildVO.Build> queryLeoJobBuildPage(LeoJobParam.JobBuildPageQuery pageQuery);


}
