package com.yy.exam.modules.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.modules.exam.dto.ExamDTO;
import com.yy.exam.modules.exam.dto.request.ExamSaveReqDTO;
import com.yy.exam.modules.exam.dto.response.ExamOnlineRespDTO;
import com.yy.exam.modules.exam.dto.response.ExamReviewRespDTO;
import com.yy.exam.modules.exam.entity.Exam;

/**
 * 考试业务接口
 * 【涉及功能：考试详情页标签页（创建/编辑考试）】
 * 
 * 定义了考试管理的所有业务方法，包括：
 * - 考试保存（含标签页数据：基本信息、题库设置）
 * - 考试详情查询（用于编辑回显，含题库信息）
 * - 考试分页查询（管理视角和学生视角）
 * - 待阅试卷查询
 * 
 * 实现类：ExamServiceImpl
 */
public interface ExamService extends IService<Exam> {

    /**
     * 保存考试信息（新增或修改）
     * 【涉及功能：考试详情页标签页 - 核心保存逻辑】
     * 
     * 一次性处理两个标签页的数据：
     * 1. 基本信息 → 存入 exam 表
     * 2. 题库设置 → 存入 exam_repo 表（先删后插）
     * 
     * 同时会自动根据题库设置计算考试总分
     * 
     * @param reqDTO 考试保存请求，包含基本信息+题库设置
     */
    void save(ExamSaveReqDTO reqDTO);

    /**
     * 查找考试完整详情（用于编辑回显）
     * 【涉及功能：考试详情页标签页 - 编辑回显】
     * 
     * 返回的数据包含两部分：
     * 1. 考试基本信息（从 exam 表）
     * 2. 题库设置列表 repoList（从 exam_repo 表）
     * 
     * 前端用这些数据分别填充两个标签页
     * 
     * @param id 考试ID
     * @return 考试完整信息（含题库设置）
     */
    ExamSaveReqDTO findDetail(String id);

    /**
     * 查找考试简要信息
     * 只返回考试基本信息，不包含题库关联
     * 用于考试进行中的快速查询
     * 
     * @param id 考试ID
     * @return 考试基本信息
     */
    ExamDTO findById(String id);

    /**
     * 考试管理分页查询（教师/管理员视角）
     * 可以查看所有考试，包括未发布、已结束的考试
     * 
     * @param reqDTO 分页请求，包含页码、每页条数、搜索条件
     * @return 分页结果
     */
    IPage<ExamDTO> paging(PagingReqDTO<ExamDTO> reqDTO);


    /**
     * 在线考试分页查询（学生视角）
     * 只显示当前时间范围内、状态正常的考试
     * 每条记录还包含学生的考试状态（未参加/进行中/已完成）
     * 
     * @param reqDTO 分页请求
     * @return 分页结果，每条记录包含考试信息和学生的考试状态
     */
    IPage<ExamOnlineRespDTO> onlinePaging(PagingReqDTO<ExamDTO> reqDTO);


    /**
     * 待阅试卷分页查询
     * 查询包含主观题且需要教师阅卷的考试列表
     * 
     * @param reqDTO 分页请求
     * @return 分页结果，含待阅数量
     */
    IPage<ExamReviewRespDTO> reviewPaging(PagingReqDTO<ExamDTO> reqDTO);
}