package com.baiyi.caesar.service.sys.impl;

import com.baiyi.caesar.common.util.IdUtil;
import com.baiyi.caesar.domain.DataTable;
import com.baiyi.caesar.domain.generator.caesar.Credential;
import com.baiyi.caesar.domain.param.sys.CredentialParam;
import com.baiyi.caesar.mapper.caesar.CredentialMapper;
import com.baiyi.caesar.service.sys.CredentialService;
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
 * @Date 2021/5/17 3:35 下午
 * @Version 1.0
 */
@Service
public class CredentialServiceImpl implements CredentialService {

    @Resource
    private CredentialMapper credentialMapper;

    @Override
    public void add(Credential credential){
        credentialMapper.insert(credential);
    }

    @Override
    public void update(Credential credential){
        credentialMapper.updateByPrimaryKey(credential);
    }

    @Override
    public void updateBySelective(Credential credential){
        credentialMapper.updateByPrimaryKeySelective(credential);
    }

    @Override
    public Credential getById(Integer id) {
        return credentialMapper.selectByPrimaryKey(id);
    }

    @Override
    public DataTable<Credential> queryPageByParam(CredentialParam.CredentialPageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPage(), pageQuery.getLength());
        Example example = new Example(Credential.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(pageQuery.getQueryName())) {
            criteria.andLike("title", SQLUtil.toLike(pageQuery.getQueryName()));
        }
        if (!IdUtil.isEmpty(pageQuery.getKind())) {
            criteria.andEqualTo("kind", pageQuery.getKind());
        }
        example.setOrderByClause("create_time");
        List<Credential> data =  credentialMapper.selectByExample(example);
        return new DataTable<>(data, page.getTotal());
    }



}
