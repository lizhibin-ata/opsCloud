package com.baiyi.caesar.service.auth.impl;

import com.baiyi.caesar.domain.DataTable;
import com.baiyi.caesar.domain.generator.caesar.AuthGroup;
import com.baiyi.caesar.domain.param.auth.AuthGroupParam;
import com.baiyi.caesar.mapper.caesar.AuthGroupMapper;
import com.baiyi.caesar.service.auth.AuthGroupService;
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
 * @Date 2021/5/11 10:08 上午
 * @Version 1.0
 */
@Service
public class AuthGroupServiceImpl implements AuthGroupService {

    @Resource
    private AuthGroupMapper authGroupMapper;

    @Override
    public AuthGroup getById(int id) {
        return authGroupMapper.selectByPrimaryKey(id);
    }

    @Override
    public DataTable<AuthGroup> queryPageByParam(AuthGroupParam.AuthGroupPageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPage(), pageQuery.getLength());
        Example example = new Example(AuthGroup.class);
        if (StringUtils.isNotBlank(pageQuery.getGroupName())) {
            Example.Criteria criteria = example.createCriteria();
            criteria.andLike("groupName", SQLUtil.toLike(pageQuery.getGroupName()));
        }
        example.setOrderByClause("create_time");
        List<AuthGroup> data = authGroupMapper.selectByExample(example);
        return new DataTable<>(data, page.getTotal());
    }

    @Override
    public void add(AuthGroup authGroup) {
        authGroupMapper.insert(authGroup);
    }

    @Override
    public void update(AuthGroup authGroup) {
        authGroupMapper.updateByPrimaryKey(authGroup);
    }

    @Override
    public void deleteById(int id) {
        authGroupMapper.deleteByPrimaryKey(id);
    }
}
