package com.yy.exam.modules.paper.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yy.exam.core.api.ApiRest;
import com.yy.exam.core.api.controller.BaseController;
import com.yy.exam.core.api.dto.BaseIdReqDTO;
import com.yy.exam.core.api.dto.BaseIdRespDTO;
import com.yy.exam.core.api.dto.BaseIdsReqDTO;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.core.utils.BeanMapper;
import com.yy.exam.modules.paper.dto.PaperDTO;
import com.yy.exam.modules.paper.dto.ext.PaperQuDetailDTO;
import com.yy.exam.modules.paper.dto.request.PaperAnswerDTO;
import com.yy.exam.modules.paper.dto.request.PaperCreateReqDTO;
import com.yy.exam.modules.paper.dto.request.PaperListReqDTO;
import com.yy.exam.modules.paper.dto.request.PaperQuQueryDTO;
import com.yy.exam.modules.paper.dto.response.ExamDetailRespDTO;
import com.yy.exam.modules.paper.dto.response.ExamResultRespDTO;
import com.yy.exam.modules.paper.dto.response.PaperListRespDTO;
import com.yy.exam.modules.paper.dto.response.PaperStatsRespDTO;
import com.yy.exam.modules.paper.entity.Paper;
import com.yy.exam.modules.paper.service.PaperService;
import com.yy.exam.modules.user.UserUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 试卷控制器
 * 所有试卷相关的HTTP接口都在这里
 * 路径前缀：/exam/api/paper/paper
 * 
 * 权限说明：
 * - sa（超级管理员）：所有接口
 * - teacher（教师）：查看成绩、成绩统计
 * - student（学生）：查看自己的成绩、参加考试
 * 
 * 涉及的7个新增功能：
 * 1. 分数排序：paging 接口的 sortField/sortOrder 参数
 * 2. 仅看不及格：paging 接口的 passFilter 参数
 * 3. 教师角色：@RequiresRoles 注解中加上 teacher
 * 5. 成绩分析：stats 接口（新建）
 * 6. 考试详情：paperDetail、paperResult 接口
 */
@Api(tags={"试卷"})
@RestController
@RequestMapping("/exam/api/paper/paper")
public class PaperController extends BaseController {

    @Autowired
    private PaperService baseService;

    /**
     * 成绩列表分页查询
     * 【涉及功能：分数排序、仅看不及格筛选、教师角色】
     * 
     * 权限：sa、teacher、student 都可以访问
     * 前端成绩列表页调用，支持按分数排序、仅看不及格等筛选
     * 
     * @param reqDTO 分页请求，包含排序字段(sortField)、排序方式(sortOrder)、是否仅看不及格(passFilter)
     * @return 分页结果
     */
    @RequiresRoles(value = {"sa", "teacher", "student"}, logical = Logical.OR)
    @ApiOperation(value = "分页查找")
    @RequestMapping(value = "/paging", method = { RequestMethod.POST})
    public ApiRest<IPage<PaperListRespDTO>> paging(@RequestBody PagingReqDTO<PaperListReqDTO> reqDTO) {
        // 分页查询并转换
        IPage<PaperListRespDTO> page = baseService.paging(reqDTO);
        return super.success(page);
    }



    /**
     * 创建试卷（学生开始考试时调用）
     * @param reqDTO
     * @return
     */
    @ApiOperation(value = "创建试卷")
    @RequestMapping(value = "/create-paper", method = { RequestMethod.POST})
    public ApiRest<BaseIdRespDTO> save(@RequestBody PaperCreateReqDTO reqDTO) {
        // 复制参数
        String paperId = baseService.createPaper(UserUtils.getUserId(), reqDTO.getExamId());
        return super.success(new BaseIdRespDTO(paperId));
    }

    /**
     * 试卷详情（考试进行中查看试卷）
     * 【涉及功能：考试详情页标签页】
     * 返回试卷基本信息 + 按题型分类的题目列表（单选、多选、判断）
     * 学生考试时调用，不显示正确答案
     * 
     * @param reqDTO 包含试卷ID
     * @return 试卷详情，包含题目列表
     */
    @ApiOperation(value = "试卷详情")
    @RequestMapping(value = "/paper-detail", method = { RequestMethod.POST})
    public ApiRest<ExamDetailRespDTO> paperDetail(@RequestBody BaseIdReqDTO reqDTO) {
        // 查询试卷详情
        ExamDetailRespDTO respDTO = baseService.paperDetail(reqDTO.getId());
        return super.success(respDTO);
    }

    /**
     * 获取单个试题的详细信息
     * @param reqDTO 包含试卷ID和试题ID
     * @return 试题详情（含选项列表）
     */
    @ApiOperation(value = "试题详情")
    @RequestMapping(value = "/qu-detail", method = { RequestMethod.POST})
    public ApiRest<PaperQuDetailDTO> quDetail(@RequestBody PaperQuQueryDTO reqDTO) {
        // 查询单个试题的详细信息
        PaperQuDetailDTO respDTO = baseService.findQuDetail(reqDTO.getPaperId(), reqDTO.getQuId());
        return super.success(respDTO);
    }

    /**
     * 填充答案（学生答题时调用）
     * @param reqDTO 包含试卷ID、试题ID、学生答案
     * @return 成功
     */
    @ApiOperation(value = "填充答案")
    @RequestMapping(value = "/fill-answer", method = { RequestMethod.POST})
    public ApiRest<PaperQuDetailDTO> fillAnswer(@RequestBody PaperAnswerDTO reqDTO) {
        // 保存学生的答案
        baseService.fillAnswer(reqDTO);
        return super.success();
    }


    /**
     * 交卷操作（学生点击交卷时调用）
     * 系统会自动计算客观题得分，主观题需要教师手动批改
     * 
     * @param reqDTO 包含试卷ID
     * @return 成功
     */
    @ApiOperation(value = "交卷操作")
    @RequestMapping(value = "/hand-exam", method = { RequestMethod.POST})
    public ApiRest<PaperQuDetailDTO> handleExam(@RequestBody BaseIdReqDTO reqDTO) {
        // 执行交卷操作
        baseService.handExam(reqDTO.getId());
        return super.success();
    }


    /**
     * 试卷结果（考试完成后查看成绩和答案）
     * 【涉及功能：考试详情页标签页 - 成绩查看】
     * 返回试卷基本信息 + 每题详情（含学生答案、正确答案、得分）
     * 
     * @param reqDTO 包含试卷ID
     * @return 试卷结果，含每题得分
     */
    @ApiOperation(value = "试卷详情")
    @RequestMapping(value = "/paper-result", method = { RequestMethod.POST})
    public ApiRest<ExamResultRespDTO> paperResult(@RequestBody BaseIdReqDTO reqDTO) {
        // 查询试卷结果
        ExamResultRespDTO respDTO = baseService.paperResult(reqDTO.getId());
        return super.success(respDTO);
    }


    /**
     * 检测用户有没有中断的考试
     * 学生登录后调用，检查是否有未完成的考试
     * 
     * @return 如果有中断的考试，返回试卷信息；否则返回空
     */
    @ApiOperation(value = "检测进行中的考试")
    @RequestMapping(value = "/check-process", method = { RequestMethod.POST})
    public ApiRest<PaperDTO> checkProcess() {
        // 查询当前用户是否有进行中的考试
        PaperDTO dto = baseService.checkProcess(UserUtils.getUserId());
        return super.success(dto);
    }

    /**
     * 成绩统计
     * 【新增功能：成绩分析弹窗】
     * 教师点击"成绩分析"按钮时调用
     * 返回该考试的成绩统计数据：总人次、及格/不及格人数、平均分、最高最低分、分数段分布
     * 
     * 权限：只有 sa 和 teacher 可以访问
     * 
     * @param reqDTO 包含考试ID
     * @return 成绩统计数据
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "成绩统计")
    @RequestMapping(value = "/stats", method = { RequestMethod.POST})
    public ApiRest<PaperStatsRespDTO> stats(@RequestBody BaseIdReqDTO reqDTO) {
        // 调用 Service 的统计方法，传入考试ID
        PaperStatsRespDTO respDTO = baseService.stats(reqDTO.getId());
        return super.success(respDTO);
    }
}