package com.yy.exam.modules.sys.dashboard.controller;

import com.yy.exam.core.api.ApiRest;
import com.yy.exam.core.api.controller.BaseController;
import com.yy.exam.modules.exam.service.ExamService;
import com.yy.exam.modules.paper.service.PaperService;
import com.yy.exam.modules.repo.service.RepoService;
import com.yy.exam.modules.sys.user.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation(value = "获取统计数据")
    @RequestMapping(value = "/stats", method = { RequestMethod.POST})
    public ApiRest<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("examCount", examService.count());
        data.put("repoCount", repoService.count());
        data.put("userCount", sysUserService.count());
        data.put("paperCount", paperService.count());
        return super.success(data);
    }
}