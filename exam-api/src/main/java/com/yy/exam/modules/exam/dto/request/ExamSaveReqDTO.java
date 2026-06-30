package com.yy.exam.modules.exam.dto.request;

import com.yy.exam.modules.exam.dto.ExamDTO;
import com.yy.exam.modules.exam.dto.ext.ExamRepoExtDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 考试保存请求DTO
 * 【涉及功能：考试详情页标签页 - 创建/编辑考试】
 * 
 * 继承 ExamDTO（包含考试基本信息：名称、时间、总分、及格线等）
 * 额外包含 repoList：题库设置列表，每个题库定义抽多少单选/多选/判断题
 * 
 * 前端考试详情页分为两个标签页：
 * "基本信息"标签页对应 ExamDTO 的字段，"题库设置"标签页对应 repoList
 * 保存时一次性提交所有标签页的数据
 */
@Data
@ApiModel(value="考试保存请求类", description="考试保存请求类")
public class ExamSaveReqDTO extends ExamDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 题库设置列表
     * 一个考试可以关联多个题库，每个题库设置不同的抽题数量
     * 示例：[{repoId:"题库1", radioCount:10, multiCount:5, judgeCount:5}, {repoId:"题库2", ...}]
     */
    @ApiModelProperty(value = "题库列表", required=true)
    private List<ExamRepoExtDTO> repoList;

}