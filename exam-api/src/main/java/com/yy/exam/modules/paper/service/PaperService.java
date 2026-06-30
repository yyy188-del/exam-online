package com.yy.exam.modules.paper.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.modules.paper.dto.PaperDTO;
import com.yy.exam.modules.paper.dto.ext.PaperQuDetailDTO;
import com.yy.exam.modules.paper.dto.request.PaperAnswerDTO;
import com.yy.exam.modules.paper.dto.request.PaperListReqDTO;
import com.yy.exam.modules.paper.dto.response.ExamDetailRespDTO;
import com.yy.exam.modules.paper.dto.response.ExamResultRespDTO;
import com.yy.exam.modules.paper.dto.response.PaperListRespDTO;
import com.yy.exam.modules.paper.dto.response.PaperStatsRespDTO;
import com.yy.exam.modules.paper.entity.Paper;

/**
 * 试卷业务接口
 * 定义了试卷管理的所有业务方法
 * 
 * 涉及的新增功能：
 * 1. 分数排序：paging 方法的 PaperListReqDTO 参数中 sortField/sortOrder 字段
 * 2. 仅看不及格筛选：paging 方法的 PaperListReqDTO 参数中 passFilter 字段
 * 5. 成绩分析弹窗：stats 方法（新建）
 * 6. 考试详情页标签页：paperDetail、paperResult 方法
 * 
 * 实现类：PaperServiceImpl
 */
public interface PaperService extends IService<Paper> {

    /**
     * 创建试卷（学生开始考试时调用）
     * 从题库中随机抽取题目，生成一份试卷
     * 
     * @param userId 学生ID
     * @param examId 考试ID
     * @return 新创建的试卷ID
     */
    String createPaper(String userId, String examId);


    /**
     * 获取试卷详情（考试进行中查看）
     * 【涉及功能：考试详情页标签页 - 学生考试视角】
     * 
     * 返回试卷基本信息 + 按题型分类的题目列表
     * 题目分为三个列表：单选题列表、多选题列表、判断题列表
     * 考试进行中不显示正确答案
     * 
     * @param paperId 试卷ID
     * @return 试卷详情，包含题目列表（按题型分类）
     */
    ExamDetailRespDTO paperDetail(String paperId);

    /**
     * 获取试卷结果（考试完成后查看成绩和答案）
     * 【涉及功能：考试详情页标签页 - 成绩查看视角】
     * 
     * 返回试卷基本信息 + 每题详情（含学生答案、正确答案、是否得分）
     * 与 paperDetail 的区别：paperDetail 不显示答案，paperResult 显示答案和得分
     * 
     * @param paperId 试卷ID
     * @return 试卷结果，含每题得分和正确答案
     */
    ExamResultRespDTO paperResult(String paperId);

    /**
     * 查找单个试题的详细信息
     * 
     * @param paperId 试卷ID
     * @param quId 试题ID
     * @return 试题详情（含选项列表）
     */
    PaperQuDetailDTO findQuDetail(String paperId, String quId);

    /**
     * 填充答案（学生答题时调用）
     * 保存学生的答案，并判断是否正确
     * 
     * @param reqDTO 包含试卷ID、试题ID、学生答案
     */
    void fillAnswer(PaperAnswerDTO reqDTO);

    /**
     * 交卷操作（学生点击交卷时调用）
     * 1. 计算客观题得分
     * 2. 如果有主观题，标记为"待阅卷"
     * 3. 同步考试成绩到 user_exam 表
     * 4. 将错题加入错题本
     * 5. 取消定时强制交卷任务
     * 
     * @param paperId 试卷ID
     */
    void handExam(String paperId);

    /**
     * 成绩列表分页查询
     * 【涉及功能：分数排序、仅看不及格筛选】
     * 
     * 支持以下筛选条件：
     * - examId：按考试筛选
     * - sortField/sortOrder：按分数排序（ASC升序/DESC降序）
     * - passFilter：仅看不及格（分数低于及格线）
     * 
     * @param reqDTO 分页请求，包含分页参数和查询条件
     * @return 分页结果
     */
    IPage<PaperListRespDTO> paging(PagingReqDTO<PaperListReqDTO> reqDTO);

    /**
     * 检测用户是否有进行中的考试
     * 学生登录后调用，检查是否有未完成的考试
     * 
     * @param userId 学生ID
     * @return 如果有进行中的考试，返回试卷信息；否则返回 null
     */
    PaperDTO checkProcess(String userId);

    /**
     * 成绩统计
     * 【新增功能：成绩分析弹窗】
     * 
     * 统计某次考试的成绩数据，包括：
     * - 总人次、及格/不及格人数
     * - 平均分、最高分、最低分
     * - 分数段分布（0-59, 60-69, 70-79, 80-89, 90-100）
     * 
     * 只统计已交卷且有分数的试卷
     * 
     * @param examId 考试ID
     * @return 成绩统计数据
     */
    PaperStatsRespDTO stats(String examId);
}