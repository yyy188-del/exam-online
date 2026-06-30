package com.yy.exam.modules.sys.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.yy.exam.core.api.ApiRest;
import com.yy.exam.core.api.controller.BaseController;
import com.yy.exam.core.api.dto.BaseIdsReqDTO;
import com.yy.exam.core.api.dto.BaseStateReqDTO;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.core.utils.excel.ExportExcel;
import com.yy.exam.core.utils.excel.ImportExcel;
import com.yy.exam.modules.sys.user.dto.SysUserDTO;
import com.yy.exam.modules.sys.user.dto.SysUserImportDTO;
import com.yy.exam.modules.sys.user.dto.request.SysUserLoginReqDTO;
import com.yy.exam.modules.sys.user.dto.request.SysUserSaveReqDTO;
import com.yy.exam.modules.sys.user.dto.response.SysUserLoginDTO;
import com.yy.exam.modules.sys.user.entity.SysUser;
import com.yy.exam.modules.sys.user.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 管理用户控制器
 * 【涉及功能：教师端学生管理】
 * 
 * 职责：处理用户管理相关的所有HTTP请求
 * 路径前缀：/exam/api/sys/user
 * 
 * 教师端学生管理功能说明：
 * - 管理员（sa）可以看到所有用户（包括管理员、教师、学生）
 * - 教师（teacher）只能看到角色为 student 的用户
 * - 教师可以对学生进行增删改查、导入导出等操作
 * - 权限控制通过 @RequiresRoles 注解 + Service 层角色过滤实现
 * 
 * 权限说明：
 * - 登录/注册/个人信息：所有用户
 * - 用户管理（增删改查）：sa 和 teacher
 * - 导入导出：sa 和 teacher
 */
@Api(tags = {"管理用户"})
@RestController
@RequestMapping("/exam/api/sys/user")
public class SysUserController extends BaseController {

    @Autowired
    private SysUserService baseService;

    /**
     * 用户登录
     * 验证用户名和密码，返回 JWT Token
     * 不需要权限注解，因为未登录用户也需要访问
     * 
     * @param reqDTO 包含用户名和密码
     * @return 登录成功返回用户信息和Token
     */
    @CrossOrigin
    @ApiOperation(value = "用户登录")
    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public ApiRest<SysUserLoginDTO> login(@RequestBody SysUserLoginReqDTO reqDTO) {
        // 调用登录服务，验证用户名密码，返回Token
        SysUserLoginDTO respDTO = baseService.login(reqDTO.getUsername(), reqDTO.getPassword());
        return super.success(respDTO);
    }

    /**
     * 用户登出
     * 清除当前用户的Token，使其失效
     * 
     * @param request HTTP请求，用于获取请求头中的Token
     * @return 成功
     */
    @CrossOrigin
    @ApiOperation(value = "用户登出")
    @RequestMapping(value = "/logout", method = {RequestMethod.POST})
    public ApiRest logout(HttpServletRequest request) {
        // 从请求头中获取Token
        String token = request.getHeader("token");
        System.out.println("+++++当前会话为："+token);
        // 使Token失效
        baseService.logout(token);
        return super.success();
    }

    /**
     * 获取会话信息（通过Token获取用户信息）
     * 前端每次刷新页面时调用，验证Token是否有效
     * 
     * @param token JWT Token
     * @return 用户信息（用户名、角色、权限等）
     */
    @ApiOperation(value = "获取会话")
    @RequestMapping(value = "/info", method = {RequestMethod.POST})
    public ApiRest info(@RequestParam("token") String token) {
        // 通过Token获取用户信息
        SysUserLoginDTO respDTO = baseService.token(token);
        return success(respDTO);
    }

    /**
     * 修改用户资料（当前登录用户修改自己的信息）
     * 
     * @param reqDTO 用户信息
     * @return 成功
     */
    @ApiOperation(value = "修改用户资料")
    @RequestMapping(value = "/update", method = {RequestMethod.POST})
    public ApiRest update(@RequestBody SysUserDTO reqDTO) {
        baseService.update(reqDTO);
        return success();
    }


    /**
     * 保存或修改系统用户
     * 【涉及功能：教师端学生管理 - 核心保存接口】
     * 
     * 教师可以通过此接口创建/编辑学生账号
     * 管理员可以通过此接口创建/编辑所有用户（包括管理员、教师、学生）
     * 
     * 权限：只有 sa 和 teacher 可以操作
     * 
     * @param reqDTO 用户保存请求，包含用户名、密码、角色等
     * @return 成功
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "保存或修改")
    @RequestMapping(value = "/save", method = {RequestMethod.POST})
    public ApiRest save(@RequestBody SysUserSaveReqDTO reqDTO) {
        baseService.save(reqDTO);
        return success();
    }


    /**
     * 批量删除用户
     * 【涉及功能：教师端学生管理】
     * 
     * 教师可以删除学生账号
     * 管理员可以删除所有用户
     * 
     * 权限：只有 sa 和 teacher 可以操作
     * 
     * @param reqDTO 包含要删除的用户ID列表
     * @return 成功
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/delete", method = { RequestMethod.POST})
    public ApiRest edit(@RequestBody BaseIdsReqDTO reqDTO) {
        // 根据ID列表批量删除
        baseService.removeByIds(reqDTO.getIds());
        return super.success();
    }

    /**
     * 用户列表分页查询
     * 【涉及功能：教师端学生管理 - 核心分页接口】
     * 
     * 角色过滤逻辑（在 Service 层实现）：
     * - 管理员（sa）：查询所有用户，不加过滤条件
     * - 教师（teacher）：只查询 role_ids 包含 'student' 的用户
     *   实际SQL：AND role_ids LIKE '%student%'
     * 
     * 这样教师登录后，在"学生管理"页面只能看到学生，看不到管理员和其他教师
     * 
     * 权限：只有 sa 和 teacher 可以访问
     * 
     * @param reqDTO 分页请求，包含页码、每页条数、搜索条件
     * @return 分页结果
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "分页查找")
    @RequestMapping(value = "/paging", method = { RequestMethod.POST})
    public ApiRest<IPage<SysUserDTO>> paging(@RequestBody PagingReqDTO<SysUserDTO> reqDTO) {
        // 分页查询，Service 层会根据当前用户角色自动过滤
        IPage<SysUserDTO> page = baseService.paging(reqDTO);
        return super.success(page);
    }

    /**
     * 修改用户状态（启用/禁用）
     * 【涉及功能：教师端学生管理】
     * 
     * 注意：admin 账号不能被禁用（SQL中加了 ne(SysUser::getUserName, "admin") 条件）
     * 
     * 权限：只有 sa 和 teacher 可以操作
     * 
     * @param reqDTO 包含用户ID列表和要设置的状态值
     * @return 成功
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ApiOperation(value = "修改状态")
    @RequestMapping(value = "/state", method = { RequestMethod.POST})
    public ApiRest state(@RequestBody BaseStateReqDTO reqDTO) {

        // 构造更新条件：WHERE id IN (id1, id2, ...) AND user_name != 'admin'
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .in(SysUser::getId, reqDTO.getIds())
                .ne(SysUser::getUserName, "admin");  // 排除admin账号，防止误操作

        // 构造更新内容：state = ?
        SysUser record = new SysUser();
        record.setState(reqDTO.getState());

        // 执行批量更新
        baseService.update(record, wrapper);

        return super.success();
    }


    /**
     * 学员注册
     * 学生自助注册账号，不需要管理员/教师审批
     * 注册后默认角色为 student
     * 
     * @param reqDTO 注册信息（用户名、密码、真实姓名等）
     * @return 注册成功返回用户信息和Token（自动登录）
     */
    @ApiOperation(value = "学员注册")
    @RequestMapping(value = "/reg", method = {RequestMethod.POST})
    public ApiRest<SysUserLoginDTO> reg(@RequestBody SysUserDTO reqDTO) {
        // 注册学生账号，自动分配 student 角色
        SysUserLoginDTO respDTO = baseService.reg(reqDTO);
        return success(respDTO);
    }

    /**
     * 快速注册（如果手机号存在则登录，不存在则注册）
     * 用于快捷登录场景，简化注册流程
     * 
     * @param reqDTO 用户信息
     * @return 用户信息和Token
     */
    @ApiOperation(value = "快速注册")
    @RequestMapping(value = "/quick-reg", method = {RequestMethod.POST})
    public ApiRest<SysUserLoginDTO> quick(@RequestBody SysUserDTO reqDTO) {
        SysUserLoginDTO respDTO = baseService.quickReg(reqDTO);
        return success(respDTO);
    }

    /**
     * 下载导入用户数据模板
     * 【涉及功能：教师端学生管理 - Excel导入】
     * 
     * 教师可以下载Excel模板，按模板填写学生信息后批量导入
     * 模板包含示例数据：用户名(zhangsan)、真实姓名(张三)、密码(123456)、角色(student)
     * 
     * 权限：只有 sa 和 teacher 可以下载模板
     * 
     * @param response HTTP响应，用于输出Excel文件
     * @return 成功
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ResponseBody
    @RequestMapping(value = "import/template")
    public ApiRest importFileTemplate(HttpServletResponse response) {
        try {
            String fileName = "用户导入模板.xlsx";
            List<SysUserImportDTO> list = Lists.newArrayList();

            // 创建示例数据，方便教师参考
            SysUserImportDTO example = new SysUserImportDTO();
            example.setUserName("zhangsan");
            example.setRealName("张三");
            example.setPassword("123456");
            example.setRole("student");

            list.add(example);

            // 导出Excel模板
            new ExportExcel("用户数据", SysUserImportDTO.class, 2).setDataList(list).write(response, fileName).dispose();
            return super.success();
        } catch (Exception e) {
            return super.failure("导入模板下载失败！失败信息：" + e.getMessage());
        }
    }

    /**
     * 导入用户Excel（批量导入学生）
     * 【涉及功能：教师端学生管理 - Excel批量导入】
     * 
     * 教师上传填写好的Excel文件，系统批量创建学生账号
     * 导入流程：
     * 1. 解析Excel文件，读取每一行数据
     * 2. 校验数据格式（用户名、密码、角色等）
     * 3. 批量插入数据库
     * 4. 返回导入结果
     * 
     * 权限：只有 sa 和 teacher 可以导入
     * 
     * @param file 上传的Excel文件
     * @return 成功或失败信息
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)
    @ResponseBody
    @RequestMapping(value = "/import")
    public ApiRest importFile(@RequestParam("file") MultipartFile file) {

        try {
            // 步骤1：解析Excel文件
            ImportExcel ei = new ImportExcel(file, 1, 0);
            List<SysUserImportDTO> list = ei.getDataList(SysUserImportDTO.class);

            // 步骤2：导入数据到数据库
            baseService.importExcel(list);

            // 步骤3：导入成功
            return super.success();

        } catch (Exception e) {
            return super.failure(e.getMessage());
        }
    }
}