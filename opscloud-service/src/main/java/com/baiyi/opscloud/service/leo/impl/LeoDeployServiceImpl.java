package com.baiyi.opscloud.service.leo.impl;

import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.LeoDeploy;
import com.baiyi.opscloud.domain.param.leo.LeoJobParam;
import com.baiyi.opscloud.domain.param.leo.request.SubscribeLeoDeployRequestParam;
import com.baiyi.opscloud.domain.vo.base.ReportVO;
import com.baiyi.opscloud.mapper.opscloud.LeoDeployMapper;
import com.baiyi.opscloud.service.leo.LeoDeployService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2022/12/5 18:02
 * @Version 1.0
 */
@Service
@RequiredArgsConstructor
public class LeoDeployServiceImpl implements LeoDeployService {

    private final LeoDeployMapper deployMapper;

    @Override
    public int getMaxDeployNumberWithJobId(Integer jobId) {
        Integer maxDeployNumber = deployMapper.getMaxDeployNumberWithJobId(jobId);
        return maxDeployNumber == null ? 0 : maxDeployNumber;
    }

    @Override
    public void add(LeoDeploy leoDeploy) {
        deployMapper.insert(leoDeploy);
    }

    @Override
    public LeoDeploy getById(Integer id) {
        return deployMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateByPrimaryKeySelective(LeoDeploy leoDeploy) {
        deployMapper.updateByPrimaryKeySelective(leoDeploy);
    }

    @Override
    public DataTable<LeoDeploy> queryDeployPage(SubscribeLeoDeployRequestParam pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPage(), pageQuery.getLength());
        Example example = new Example(LeoDeploy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("jobId", pageQuery.getJobIds());
        example.setOrderByClause("id desc");
        List<LeoDeploy> data = deployMapper.selectByExample(example);
        return new DataTable<>(data, page.getTotal());
    }

    @Override
    public DataTable<LeoDeploy> queryDeployPage(LeoJobParam.JobDeployPageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPage(), pageQuery.getLength());
        Example example = new Example(LeoDeploy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("jobId", pageQuery.getJobIds());
        if (StringUtils.isNotBlank(pageQuery.getDeployResult())) {
            criteria.andEqualTo("deployResult", pageQuery.getDeployResult());
        }
        example.setOrderByClause("id desc");
        List<LeoDeploy> data = deployMapper.selectByExample(example);
        return new DataTable<>(data, page.getTotal());
    }

    @Override
    public List<LeoDeploy> queryNotFinishDeployWithOcInstance(String ocInstance) {
        Example example = new Example(LeoDeploy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isFinish", false)
                //.andEqualTo("isActive", false)
                .andEqualTo("ocInstance", ocInstance);
        example.setOrderByClause("id desc");
        return deployMapper.selectByExample(example);
    }

    @Override
    public List<LeoDeploy> queryWithJobId(Integer jobId) {
        Example example = new Example(LeoDeploy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("jobId", jobId);
        return deployMapper.selectByExample(example);
    }

    @Override
    public List<LeoDeploy> queryWithBuildId(Integer buildId) {
        Example example = new Example(LeoDeploy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("buildId", buildId);
        return deployMapper.selectByExample(example);
    }

    @Override
    public int countRunningWithJobId(int jobId) {
        Example example = new Example(LeoDeploy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("jobId", jobId)
                .andEqualTo("isActive", true)
                .andEqualTo("isFinish", false);
        return deployMapper.selectCountByExample(example);
    }

    @Override
    public int countWithJobId(int jobId) {
        Example example = new Example(LeoDeploy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("jobId", jobId);
        return deployMapper.selectCountByExample(example);
    }

    @Override
    public List<ReportVO.Report> statByMonth() {
        return deployMapper.statByMonth();
    }

    @Override
    public int countWithReport() {
        Example example = new Example(LeoDeploy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isActive", true)
                .andEqualTo("isFinish", true);
        return deployMapper.selectCountByExample(example);
    }

    @Override
    public void deleteById(Integer id) {
        deployMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<ReportVO.Report> statByEnvName() {
        return deployMapper.statByEnvName();
    }

}
