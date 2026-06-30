package com.yy.exam.modules.paper.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 试卷列表查询请求DTO
 * 前端成绩列表页发送查询请求时，所有查询条件都通过这个类传递给后端
 * 包含了分页、筛选、排序等所有参数
 */
@Data
@ApiModel(value="试卷", description="试卷")
public class PaperListReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID", required=true)
    private String userId;

    @ApiModelProperty(value = "规则ID（考试ID）", required=true)
    private String examId;

    @ApiModelProperty(value = "用户昵称（用于模糊搜索学生姓名）", required=true)
    private String realName;

    @ApiModelProperty(value = "试卷状态（1=进行中, 2=已完成）", required=true)
    private Integer state;

    /**
     * 【新增功能：仅看不及格筛选】
     * 值为1时，后端SQL会加上条件：state=2（已交卷）且 user_score < qualify_score（分数低于及格线）
     * 不传或传null时，不筛选，返回所有成绩
     */
    @ApiModelProperty(value = "仅看不及格:1")
    private Integer passFilter;

    /**
     * 【新增功能：分数排序】
     * 前端点击表头时传过来，比如 'userScore' 表示按分数排序
     * 后端XML根据这个字段拼接 ORDER BY 子句
     */
    @ApiModelProperty(value = "排序字段")
    private String sortField;

    /**
     * 【新增功能：分数排序】
     * 排序方式：ASC（升序，从低到高）或 DESC（降序，从高到低）
     * 前端Element UI表头点击时发送 'ascending' 或 'descending'，前端转换后传给后端
     */
    @ApiModelProperty(value = "排序方式")
    private String sortOrder;

    
}