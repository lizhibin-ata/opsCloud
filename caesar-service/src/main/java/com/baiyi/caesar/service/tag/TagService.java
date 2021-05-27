package com.baiyi.caesar.service.tag;

import com.baiyi.caesar.domain.DataTable;
import com.baiyi.caesar.domain.generator.caesar.Tag;
import com.baiyi.caesar.domain.param.tag.TagParam;

import java.util.List;

/**
 * @Author baiyi
 * @Date 2021/5/19 2:26 下午
 * @Version 1.0
 */
public interface TagService {

    List<Tag> queryBusinessTagByParam(TagParam.BusinessQuery queryParam);

    List<Tag> queryTagByBusinessType(Integer businessType);

    Tag getById(Integer id);

    void add(Tag tag);

    void update(Tag tag);

    Tag getByTagKey(String tagKey);

    DataTable<Tag> queryPageByParam(TagParam.TagPageQuery pageQuery);
}
