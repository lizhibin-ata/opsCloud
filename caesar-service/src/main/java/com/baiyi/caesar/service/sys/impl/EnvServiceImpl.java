package com.baiyi.caesar.service.sys.impl;

import com.baiyi.caesar.common.util.IdUtil;
import com.baiyi.caesar.domain.DataTable;
import com.baiyi.caesar.domain.generator.caesar.Env;
import com.baiyi.caesar.domain.param.sys.EnvParam;
import com.baiyi.caesar.mapper.caesar.EnvMapper;
import com.baiyi.caesar.service.sys.EnvService;
import com.baiyi.caesar.util.SQLUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/5/25 4:34 下午
 * @Version 1.0
 */
@Service
public class EnvServiceImpl implements EnvService {

    @Resource
    private EnvMapper envMapper;

    @Override
    public void add(Env env) {
        envMapper.insert(env);
    }

    @Override
    public void update(Env env) {
        envMapper.updateByPrimaryKey(env);
    }

    @Override
    public DataTable<Env> queryPageByParam(EnvParam.EnvPageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPage(), pageQuery.getLength());
        Example example = new Example(Env.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(pageQuery.getEnvName())) {
            criteria.andLike("envName", SQLUtil.toLike(pageQuery.getEnvName()));
        }
        if (IdUtil.isNotEmpty(pageQuery.getEnvType())) {
            criteria.andEqualTo("envType", pageQuery.getEnvType());
        }
        example.setOrderByClause("create_time");
        List<Env> data = envMapper.selectByExample(example);
        return new DataTable<>(data, page.getTotal());
    }

    @Override
    public Env getByEnvType(Integer envType) {
        Example example = new Example(Env.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("envType", envType);
        return envMapper.selectOneByExample(example);
    }
}
