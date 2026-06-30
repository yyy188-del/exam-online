package com.yy.exam.modules.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yy.exam.core.api.ApiRest;
import com.yy.exam.core.api.controller.BaseController;
import com.yy.exam.core.api.dto.BaseIdReqDTO;
import org.apache.shiro.authz.annotation.Logical;
import com.yy.exam.core.api.dto.BaseIdsReqDTO;
import com.yy.exam.core.api.dto.BaseStateReqDTO;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.modules.exam.dto.ExamDTO;
import com.yy.exam.modules.exam.dto.request.ExamSaveReqDTO;
import com.yy.exam.modules.exam.dto.response.ExamOnlineRespDTO;
import com.yy.exam.modules.exam.dto.response.ExamReviewRespDTO;
import com.yy.exam.modules.exam.entity.Exam;
import com.yy.exam.modules.exam.service.ExamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 考试控制器
 * 【涉及功能：考试详情页标签页（创建/编辑考试）】
 * 
 * 职责：处理所有考试相关的HTTP请求
 * 路径前缀：/exam/api/exam/exam
 * 
 * 考试详情页标签页功能说明：
 * - 创建/编辑考试时，前端使用标签页（Tabs）组织表单
 * - 标签页1：基本信息（考试名称、时间、总分、及格线等）
 * - 标签页2：题库设置（关联哪些题库、每个题库抽多少题）
 * - 保存时一次性提交所有标签页的数据，后端统一处理
 * 
 * 权限说明：
 * - sa（超级管理员）：所有接口
 * - teacher（教师）：创建/编辑/删除考试、查看成绩
 * - student（学生）：只能查看在线考试列表
 */
@Api(tags={"考试"})
@RestController
@RequestMapping("/exam/api/exam/exam")
public class ExamController extends BaseController {

    @Autowired
    private ExamService baseService;

    /**
     * 添加或修改考试
     * 【涉及功能：考试详情页标签页 - 核心保存接口】
     * 
     * 前端提交的数据包含两个标签页的全部内容：
     * 1. 基本信息：考试名称(title)、考试描述(content)、开始/结束时间、总分、及格线等
     * 2. 题库设置(repoList)：[{repoId:"题库1", radioCount:10, multiCount:5, judgeCount:5}, ...]
     * 
     * 后端处理流程：
     * 1. 判断是新增还是修改（有ID=修改，无ID=新增）
     * 2. 根据题库设置计算总分（单选数量×单选分数 + 多选数量×多选分数 + 判断数量×判断分数）
     * 3. 保存考试基本信息到 exam 表
     * 4. 保存题库关联到 exam_repo 表（先删后插）
     * 
     * 权限：只有 sa 和 teacher 可以创建/编辑考试
     * 
     * @param reqDTO 考试保存请求，包含基本信息+题库设置
     * @return 成功
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "添加或修改")
    @RequestMapping(value = "/save", method = { RequestMethod.POST})
    public ApiRest save(@RequestBody ExamSaveReqDTO reqDTO) {
        // 调用 Service 的 save 方法，统一处理考试基本信息、题库关联
        baseService.save(reqDTO);
        return super.success();
    }

    /**
     * 批量删除考试
     * 
     * 权限：只有 sa 和 teacher 可以删除考试
     * 
     * @param reqDTO 包含要删除的考试ID列表
     * @return 成功
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/delete", method = { RequestMethod.POST})
    public ApiRest edit(@RequestBody BaseIdsReqDTO reqDTO) {
        // 根据ID列表批量删除（MyBatis-Plus 的 removeByIds 方法）
        baseService.removeByIds(reqDTO.getIds());
        return super.success();
    }

    /**
     * 查找考试详情（用于编辑考试时的回显）
     * 【涉及功能：考试详情页标签页 - 编辑回显】
     * 
     * 教师点击"编辑"按钮时调用此接口，返回的数据会填充到三个标签页中：
     * 1. 基本信息：考试名称、时间、总分、及格线等 → 填充到"基本信息"标签页
     * 2. 题库列表(repoList)：该考试关联的题库及抽题配置 → 填充到"题库设置"标签页
     * 3. 部门列表(departIds)：该考试开放的部门 → 填充到"指定部门"标签页
     * 
     * 返回类型是 ExamSaveReqDTO，和保存时用的同一个DTO，保证数据结构一致
     * 
     * @param reqDTO 包含考试ID
     * @return 考试完整信息（含题库设置和部门列表）
     */
    @ApiOperation(value = "查找详情")
    @RequestMapping(value = "/detail", method = { RequestMethod.POST})
    public ApiRest<ExamSaveReqDTO> find(@RequestBody BaseIdReqDTO reqDTO) {
        // 查询考试基本信息 + 题库列表 + 部门列表，组装成完整的 ExamSaveReqDTO
        ExamSaveReqDTO dto = baseService.findDetail(reqDTO.getId());
        return super.success(dto);
    }

    /**
     * 批量修改考试状态（启用/禁用）
     * 
     * 权限：只有 sa 和 teacher 可以修改考试状态
     * 
     * @param reqDTO 包含考试ID列表和要设置的状态值
     * @return 成功
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "修改状态")
    @RequestMapping(value = "/state", method = { RequestMethod.POST})
    public ApiRest state(@RequestBody BaseStateReqDTO reqDTO) {
        // 构造更新条件：WHERE id IN (id1, id2, ...)
        QueryWrapper<Exam> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(Exam::getId, reqDTO.getIds());

        // 构造更新内容：state = ?, update_time = 当前时间
        Exam exam = new Exam();
        exam.setState(reqDTO.getState());
        exam.setUpdateTime(new Date());

        // 执行批量更新
        baseService.update(exam, wrapper);
        return super.success();
    }


    /**
     * 在线考试分页查询（学生视角）
     * 
     * 学生登录后，在"在线考试"页面看到的考试列表
     * 只显示当前时间范围内、状态正常的考试
     * 不需要权限注解，因为所有登录用户都可以访问
     * 
     * @param reqDTO 分页请求，包含页码、每页条数、搜索条件
     * @return 分页结果，每条记录包含考试基本信息和学生的考试状态
     */
    @ApiOperation(value = "考试视角")
    @RequestMapping(value = "/online-paging", method = { RequestMethod.POST})
    public ApiRest<IPage<ExamOnlineRespDTO>> myPaging(@RequestBody PagingReqDTO<ExamDTO> reqDTO) {
        // 分页查询在线考试列表（学生视角）
        IPage<ExamOnlineRespDTO> page = baseService.onlinePaging(reqDTO);
        return super.success(page);
    }

    /**
     * 考试管理分页查询（教师/管理员视角）
     * 
     * 教师和管理员在"考试管理"页面看到的考试列表
     * 可以查看所有考试，包括未发布、已结束的考试
     * 
     * 权限：只有 sa 和 teacher 可以访问考试管理
     * 
     * @param reqDTO 分页请求，包含页码、每页条数、搜索条件
     * @return 分页结果
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "分页查找")
    @RequestMapping(value = "/paging", method = { RequestMethod.POST})
    public ApiRest<IPage<ExamDTO>> paging(@RequestBody PagingReqDTO<ExamDTO> reqDTO) {
        // 分页查询考试列表（管理视角）
        IPage<ExamDTO> page = baseService.paging(reqDTO);
        return super.success(page);
    }


    /**
     * 待阅试卷分页查询
     * 
     * 教师批改主观题时使用，列出所有需要阅卷的试卷
     * 只有包含主观题的考试才会出现在这里
     * 
     * 权限：只有 sa 和 teacher 可以阅卷
     * 
     * @param reqDTO 分页请求
     * @return 分页结果，每条记录包含考试信息和待阅数量
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "待阅试卷")
    @RequestMapping(value = "/review-paging", method = { RequestMethod.POST})
    public ApiRest<IPage<ExamReviewRespDTO>> reviewPaging(@RequestBody PagingReqDTO<ExamDTO> reqDTO) {
        // 分页查询待阅卷的考试列表
        IPage<ExamReviewRespDTO> page = baseService.reviewPaging(reqDTO);
        return super.success(page);
    }


}