package com.baiyi.opscloud.service.application.impl;

import com.baiyi.opscloud.domain.generator.opscloud.ApplicationResource;
import com.baiyi.opscloud.mapper.opscloud.ApplicationResourceMapper;
import com.baiyi.opscloud.service.application.ApplicationResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/7/13 9:30 上午
 * @Version 1.0
 */
@Service
@RequiredArgsConstructor
public class ApplicationResourceServiceImpl implements ApplicationResourceService {

    private final ApplicationResourceMapper applicationResourceMapper;

    @Override
    public ApplicationResource getById(Integer id) {
        return applicationResourceMapper.selectByPrimaryKey(id);
    }

    @Override
    public void add(ApplicationResource applicationResource) {
        applicationResourceMapper.insert(applicationResource);
    }

    @Override
    public void update(ApplicationResource applicationResource) {
        applicationResourceMapper.updateByPrimaryKey(applicationResource);
    }

    @Override
    public void delete(Integer id) {
        applicationResourceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public ApplicationResource getByUniqueKey(Integer applicationId, Integer businessType, Integer businessId) {
        Example example = new Example(ApplicationResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("applicationId", applicationId)
                .andEqualTo("businessType", businessType)
                .andEqualTo("businessId", businessId);
        return applicationResourceMapper.selectOneByExample(example);
    }

    @Override
    public List<ApplicationResource> queryByApplication(Integer applicationId) {
        Example example = new Example(ApplicationResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("applicationId", applicationId);
        return applicationResourceMapper.selectByExample(example);
    }

    @Override
    public List<ApplicationResource> queryByApplication(Integer applicationId, String resourceType) {
        Example example = new Example(ApplicationResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("applicationId", applicationId)
                .andEqualTo("resourceType", resourceType);
        return applicationResourceMapper.selectByExample(example);
    }

    @Override
    public List<ApplicationResource> queryByApplication(Integer applicationId, String resourceType, int businessType) {
        Example example = new Example(ApplicationResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("applicationId", applicationId)
                .andEqualTo("resourceType", resourceType)
                .andEqualTo("businessType", businessType);
        return applicationResourceMapper.selectByExample(example);
    }

    @Override
    public List<ApplicationResource> queryByBusiness(Integer businessType, Integer businessId) {
        Example example = new Example(ApplicationResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("businessType", businessType)
                .andEqualTo("businessId", businessId);
        return applicationResourceMapper.selectByExample(example);
    }
}
