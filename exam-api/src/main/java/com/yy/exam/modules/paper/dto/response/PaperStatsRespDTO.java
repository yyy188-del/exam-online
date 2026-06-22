package com.yy.exam.modules.paper.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "成绩统计", description = "成绩统计")
public class PaperStatsRespDTO implements Serializable {

    @ApiModelProperty(value = "总人次")
    private Integer totalPapers;

    @ApiModelProperty(value = "及格人数")
    private Integer passCount;

    @ApiModelProperty(value = "不及格人数")
    private Integer failCount;

    @ApiModelProperty(value = "平均分")
    private Double avgScore;

    @ApiModelProperty(value = "最高分")
    private Integer maxScore;

    @ApiModelProperty(value = "最低分")
    private Integer minScore;

    @ApiModelProperty(value = "分数段分布")
    private List<ScoreRange> distribution;

    @Data
    public static class ScoreRange implements Serializable {
        private String label;
        private Integer count;
    }
}