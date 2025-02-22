package com.baiyi.opscloud.domain.vo.terminal;

import com.baiyi.opscloud.domain.vo.base.ReportVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author baiyi
 * @Date 2022/7/7 20:47
 * @Version 1.0
 */
public class TerminalReportVO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class TerminalReport implements Serializable {

        private static final long serialVersionUID = 6948099219656277502L;

        @ApiModelProperty(value = "用户总数")
        private Integer userTotal;

        @ApiModelProperty(value = "会话总数")
        private Integer sessionTotal;

        @ApiModelProperty(value = "实例总数")
        private Integer instanceTotal;

        @ApiModelProperty(value = "命令总数")
        private Integer commandTotal;

        private ReportVO.MonthlyReport sessionReport;

        private ReportVO.MonthlyReport instanceReport;

        private ReportVO.MonthReport commandMonthReport;

    }



}
