package com.baiyi.caesar.common.annotation;

import com.baiyi.caesar.domain.types.BusinessTypeEnum;

import java.lang.annotation.*;

/**
 * @Author baiyi
 * @Date 2021/5/25 8:56 上午
 * @Version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TagClear {

    BusinessTypeEnum type();
}
