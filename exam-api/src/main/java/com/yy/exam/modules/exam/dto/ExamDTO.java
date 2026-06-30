package com.yy.exam.modules.exam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yy.exam.modules.paper.enums.ExamState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 考试数据传输类（DTO）
 * 【涉及功能：考试详情页标签页】
 * 
 * 这是考试基本信息的数据传输对象，是 ExamSaveReqDTO 的父类
 * 
 * 继承关系：
 * ExamDTO（基本信息） → ExamSaveReqDTO（基本信息 + 题库设置）
 * 
 * 在考试详情页标签页中：
 * - "基本信息"标签页的所有字段都来自 ExamDTO
 * - "题库设置"标签页的数据来自 ExamSaveReqDTO.repoList
 * 
 * 字段说明：
 * - title：考试名称，如"2024年Java期末考试"
 * - content：考试描述/说明，如"本次考试共100分，90分钟完成"
 * - state：考试状态（0=未开始，1=进行中，2=已禁用）
 * - timeLimit：是否限时（true=限时考试，false=不限时）
 * - startTime/endTime：考试开始/结束时间
 * - totalScore：考试总分（由系统根据题库设置自动计算）
 * - totalTime：考试时长（分钟）
 * - qualifyScore：及格分数线
 */
@Data
@ApiModel(value="考试", description="考试")
public class ExamDTO implements Serializable {


    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "ID", required=true)
    private String id;

    @ApiModelProperty(value = "考试名称", required=true)
    private String title;

    @ApiModelProperty(value = "考试描述", required=true)
    private String content;

    @ApiModelProperty(value = "1公开2部门3定员", required=true)
    private Integer openType;

    @ApiModelProperty(value = "考试状态", required=true)
    private Integer state;

    @ApiModelProperty(value = "是否限时", required=true)
    private Boolean timeLimit;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "开始时间", required=true)
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "结束时间", required=true)
    private Date endTime;

    @ApiModelProperty(value = "创建时间", required=true)
    private Date createTime;

    @ApiModelProperty(value = "更新时间", required=true)
    private Date updateTime;

    @ApiModelProperty(value = "总分数", required=true)
    private Integer totalScore;

    @ApiModelProperty(value = "总时长（分钟）", required=true)
    private Integer totalTime;

    @ApiModelProperty(value = "及格分数", required=true)
    private Integer qualifyScore;


    /**
     * 获取考试的实际状态（动态计算）
     * 
     * 对于限时考试，状态不是固定的，而是根据当前时间动态计算：
     * - 当前时间 < 开始时间 → 未开始（READY_START）
     * - 当前时间 > 结束时间 → 已结束（OVERDUE）
     * - 开始时间 < 当前时间 < 结束时间 且 未被禁用 → 进行中（ENABLE）
     * 
     * 对于不限时考试，直接返回数据库中的状态值
     * 
     * @return 考试的实际状态码
     */
    public Integer getState(){

        // 只对限时考试进行动态状态计算
        if(this.timeLimit!=null && this.timeLimit){

            // 当前时间 < 开始时间：考试还未开始
            if(System.currentTimeMillis() < startTime.getTime() ){
                return ExamState.READY_START;
            }

            // 当前时间 > 结束时间：考试已过期
            if(System.currentTimeMillis() > endTime.getTime()){
                return ExamState.OVERDUE;
            }

            // 开始时间 < 当前时间 < 结束时间 且 未被禁用：考试进行中
            if(System.currentTimeMillis() > startTime.getTime()
                    && System.currentTimeMillis() < endTime.getTime()
                    && !ExamState.DISABLED.equals(this.state)){
                return ExamState.ENABLE;
            }

        }

        // 不限时考试或限时考试被禁用时，直接返回数据库中的状态值
        return this.state;
    }
}