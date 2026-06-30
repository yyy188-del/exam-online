package com.yy.exam.modules.paper.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 成绩统计响应DTO
 * 【新增功能：成绩分析弹窗】
 * 
 * 用于返回某次考试的成绩统计数据，包括：
 * - 总人次、及格/不及格人数
 * - 平均分、最高分、最低分
 * - 分数段分布（0-59, 60-69, 70-79, 80-89, 90-100）
 * 
 * 前端拿到这个数据后，用弹窗+图表展示
 */
@Data
@ApiModel(value = "成绩统计", description = "成绩统计")
public class PaperStatsRespDTO implements Serializable {

    @ApiModelProperty(value = "总人次（参加该考试的学生总数）")
    private Integer totalPapers;

    @ApiModelProperty(value = "及格人数（分数 >= 及格线的人数）")
    private Integer passCount;

    @ApiModelProperty(value = "不及格人数（分数 < 及格线的人数）")
    private Integer failCount;

    @ApiModelProperty(value = "平均分（总分 / 已完成人数，保留一位小数）")
    private Double avgScore;

    @ApiModelProperty(value = "最高分（所有已交卷试卷中的最高分）")
    private Integer maxScore;

    @ApiModelProperty(value = "最低分（所有已交卷试卷中的最低分）")
    private Integer minScore;

    /**
     * 分数段分布列表
     * 每个元素包含：label（分数段名称，如 "0-59"）和 count（该分数段人数）
     * 示例：[{label:"0-59", count:5}, {label:"60-69", count:10}, ...]
     */
    @ApiModelProperty(value = "分数段分布")
    private List<ScoreRange> distribution;

    /**
     * 分数段内部类
     * 表示一个分数段及该分数段的人数
     */
    @Data
    public static class ScoreRange implements Serializable {
        /** 分数段名称，如 "0-59", "60-69" */
        private String label;
        /** 该分数段的人数 */
        private Integer count;
    }
}