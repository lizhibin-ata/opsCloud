package com.baiyi.opscloud.sshserver.annotation;

import java.lang.annotation.*;

/**
 *
 * 检查终端窗体Size
 *
 * @Author baiyi
 * @Date 2021/7/9 10:13 上午
 * @Version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CheckTerminalSize {

    int cols() default 0;
    int rows() default 0;
}
