package com.baiyi.opscloud.domain.constants;

import lombok.Getter;

import java.util.Arrays;

/**
 * @Author baiyi
 * @Date 2020/2/22 5:46 下午
 * @Version 1.0
 */
@Getter
public enum BusinessTypeEnum {

    COMMON(0, "通用"),
    SERVER(1, "服务器", true),
    SERVERGROUP(2, "服务器组", true),
    USER(3, "用户"),
    USERGROUP(4, "用户组"),
    ASSET(5, "资产", true),
    SERVER_ADMINISTRATOR_ACCOUNT(6, "服务器器管理员账户"),
    APPLICATION(8, "应用"),
    DATASOURCE_INSTANCE(16, "数据源实例", true),
    LEO_TEMPLATE(20, "持续交付模板"),
    LEO_JOB(21, "持续交付作业"),
    USER_PERMISSION(100, "用户授权"),
    BUSINESS_ASSET_RELATION(101, "业务资产关系"),
    WORK_EVENT(102, "工作事件");

    private final String name;
    private final int type;
    private final boolean inApplication;

    public static BusinessTypeEnum getByType(int type) {
        return Arrays.stream(BusinessTypeEnum.values()).filter(typeEnum -> typeEnum.type == type).findFirst().orElse(null);
    }

    BusinessTypeEnum(int type, String name, boolean inApplication) {
        this.type = type;
        this.name = name;
        this.inApplication = inApplication;
    }

    BusinessTypeEnum(int type, String name) {
        this(type, name, false);
    }
}
