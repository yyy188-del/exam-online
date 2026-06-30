package com.yy.exam.modules.sys.dashboard.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yy.exam.ability.shiro.jwt.JwtUtils;
import com.yy.exam.core.api.ApiRest;
import com.yy.exam.core.api.controller.BaseController;
import com.yy.exam.modules.Constant;
import com.yy.exam.modules.exam.service.ExamService;
import com.yy.exam.modules.paper.service.PaperService;
import com.yy.exam.modules.qu.service.QuService;
import com.yy.exam.modules.repo.service.RepoService;
import com.yy.exam.modules.sys.user.entity.SysUser;
import com.yy.exam.modules.sys.user.service.SysUserRoleService;
import com.yy.exam.modules.sys.user.service.SysUserService;
import com.yy.exam.modules.user.exam.entity.UserExam;
import com.yy.exam.modules.user.exam.service.UserExamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘统计控制器
 * 【新增功能：角色化仪表盘 - 核心模块】
 * 
 * 职责：根据当前登录用户的角色，返回不同的统计数据
 * 
 * 三种角色看到的数据：
 * ┌──────────┬──────────────────────────────────────┐
 * │ 超管(sa)  │ 考试数、题库数、用户数、试卷数、试题数  │
 * │ 教师      │ 考试数、题库数、试题数、试卷数、报名数  │
 * │ 学生      │ 我的考试数、通过数、全部考试数         │
 * └──────────┴──────────────────────────────────────┘
 * 
 * 认证流程：
 * 1. 从HTTP请求头中取出JWT Token
 * 2. 解析Token得到用户名
 * 3. 查数据库获取用户信息
 * 4. 判断用户角色（isAdmin、isTeacher）
 * 5. 根据角色返回不同维度的统计数据
 * 
 * 注意：这个接口没有 @RequiresRoles 注解，因为所有角色都要访问
 * 认证由 Shiro 的全局 JWT 过滤器保证
 */
@Api(tags={"仪表盘统计"})
@RestController
@RequestMapping("/exam/api/sys/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private ExamService examService;           // 考试服务（查考试数量）

    @Autowired
    private RepoService repoService;           // 题库服务（查题库数量）

    @Autowired
    private SysUserService sysUserService;     // 用户服务（查用户数量、查用户信息）

    @Autowired
    private PaperService paperService;         // 试卷服务（查试卷数量）

    @Autowired
    private UserExamService userExamService;   // 用户考试关联服务（查报名数、通过数）

    @Autowired
    private QuService quService;               // 试题服务（查试题数量）

    @Autowired
    private SysUserRoleService sysUserRoleService; // 角色服务（判断用户角色）

    /**
     * 获取仪表盘统计数据
     * 【新增功能：角色化仪表盘】
     * 
     * 不需要传参数，从Token中自动解析当前用户
     * 根据用户角色返回不同的统计数据
     * 
     * 完整流程：
     * 1. 从请求头获取JWT Token
     * 2. 解析Token → 得到用户名
     * 3. 查 sys_user 表 → 得到用户ID
     * 4. 查 sys_user_role 表 → 判断角色（admin? teacher? student?）
     * 5. 根据角色查不同表 → 统计不同维度的数据
     * 6. 组装成 Map 返回给前端
     * 
     * @param request HTTP请求对象，用于获取请求头中的Token
     * @return 统计数据Map，key是统计项名称，value是数量
     */
    @ApiOperation(value = "获取统计数据")
    @RequestMapping(value = "/stats", method = { RequestMethod.POST})
    public ApiRest<Map<String, Object>> stats(HttpServletRequest request) {

        // ===== 第1步：从请求头中获取JWT Token =====
        // Constant.TOKEN 是请求头名称，通常是 "X-Token" 或 "Authorization"
        String token = request.getHeader(Constant.TOKEN);
        if (token == null) {
            // 如果请求头没有，尝试从URL参数获取（兼容某些特殊情况）
            token = request.getParameter("token");
        }

        // ===== 第2步：解析Token，获取用户名 =====
        // JwtUtils.getUsername() 用密钥解密Token，取出里面存储的 username
        String username = JwtUtils.getUsername(token);

        // ===== 第3步：根据用户名查数据库，获取用户完整信息 =====
        QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
        userWrapper.lambda().eq(SysUser::getUserName, username);
        SysUser user = sysUserService.getOne(userWrapper);

        // 如果用户不存在（不太可能，但防御性编程），返回空数据
        if (user == null) {
            return super.success(new HashMap<>());
        }

        String userId = user.getId();
        Map<String, Object> data = new HashMap<>();  // 存放返回数据

        // ===== 第4步：判断用户角色 =====
        boolean isAdmin = sysUserRoleService.isAdmin(userId);      // 是不是超管？
        boolean isTeacher = sysUserRoleService.isTeacher(userId);  // 是不是教师？

        // ===== 第5步：根据角色返回不同数据 =====

        // 分支1：超级管理员 —— 看到全量数据
        if (isAdmin) {
            data.put("examCount", examService.count());      // 考试总数
            data.put("repoCount", repoService.count());      // 题库总数
            data.put("userCount", sysUserService.count());   // 用户总数
            data.put("paperCount", paperService.count());    // 试卷总数
            data.put("quCount", quService.count());          // 试题总数
            return super.success(data);
        }

        // 分支2：教师 —— 看到教学相关的数据（没有用户总数）
        if (isTeacher) {
            data.put("examCount", examService.count());          // 考试总数
            data.put("repoCount", repoService.count());          // 题库总数
            data.put("quCount", quService.count());              // 试题总数
            data.put("paperCount", paperService.count());        // 试卷总数
            data.put("userExamCount", userExamService.count());  // 报名人数
            return super.success(data);
        }

        // 分支3：学生 —— 看到自己的学习数据
        // 查询我参加的考试数
        QueryWrapper<UserExam> userExamWrapper = new QueryWrapper<>();
        userExamWrapper.lambda().eq(UserExam::getUserId, userId);
        data.put("myExamCount", userExamService.count(userExamWrapper));

        // 查询我通过的考试数（passed = true）
        QueryWrapper<UserExam> passedWrapper = new QueryWrapper<>();
        passedWrapper.lambda().eq(UserExam::getUserId, userId).eq(UserExam::getPassed, true);
        data.put("passedCount", userExamService.count(passedWrapper));

        // 全部考试数
        data.put("examCount", examService.count());

        return super.success(data);
    }
}