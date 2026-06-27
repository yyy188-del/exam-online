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

@Api(tags={"仪表盘统计"})
@RestController
@RequestMapping("/exam/api/sys/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private ExamService examService;

    @Autowired
    private RepoService repoService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private UserExamService userExamService;

    @Autowired
    private QuService quService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @ApiOperation(value = "获取统计数据")
    @RequestMapping(value = "/stats", method = { RequestMethod.POST})
    public ApiRest<Map<String, Object>> stats(HttpServletRequest request) {

        String token = request.getHeader(Constant.TOKEN);
        if (token == null) {
            token = request.getParameter("token");
        }

        String username = JwtUtils.getUsername(token);
        QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
        userWrapper.lambda().eq(SysUser::getUserName, username);
        SysUser user = sysUserService.getOne(userWrapper);

        if (user == null) {
            return super.success(new HashMap<>());
        }

        String userId = user.getId();
        Map<String, Object> data = new HashMap<>();

        boolean isAdmin = sysUserRoleService.isAdmin(userId);
        boolean isTeacher = sysUserRoleService.isTeacher(userId);

        // 超管：返回全量数据
        if (isAdmin) {
            data.put("examCount", examService.count());
            data.put("repoCount", repoService.count());
            data.put("userCount", sysUserService.count());
            data.put("paperCount", paperService.count());
            data.put("quCount", quService.count());
            return super.success(data);
        }

        // 教师：返回自己的数据
        if (isTeacher) {
            data.put("examCount", examService.count());
            data.put("repoCount", repoService.count());
            data.put("quCount", quService.count());
            data.put("paperCount", paperService.count());
            data.put("userExamCount", userExamService.count());
            return super.success(data);
        }

        // 学生：返回学生相关数据
        QueryWrapper<UserExam> userExamWrapper = new QueryWrapper<>();
        userExamWrapper.lambda().eq(UserExam::getUserId, userId);
        data.put("myExamCount", userExamService.count(userExamWrapper));

        QueryWrapper<UserExam> passedWrapper = new QueryWrapper<>();
        passedWrapper.lambda().eq(UserExam::getUserId, userId).eq(UserExam::getPassed, true);
        data.put("passedCount", userExamService.count(passedWrapper));

        data.put("examCount", examService.count());
        return super.success(data);
    }
}