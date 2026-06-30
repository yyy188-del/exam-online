package com.yy.exam.modules.sys.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.exam.core.api.ApiError;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.core.enums.CommonState;
import com.yy.exam.core.exception.ServiceException;
import com.yy.exam.core.utils.BeanMapper;
import com.yy.exam.core.utils.passwd.PassHandler;
import com.yy.exam.core.utils.passwd.PassInfo;
import com.yy.exam.ability.shiro.jwt.JwtUtils;
import com.yy.exam.modules.sys.user.dto.SysUserDTO;
import com.yy.exam.modules.sys.user.dto.SysUserImportDTO;
import com.yy.exam.modules.sys.user.dto.request.SysUserSaveReqDTO;
import com.yy.exam.modules.sys.user.dto.response.SysUserLoginDTO;
import com.yy.exam.modules.sys.user.entity.SysUser;
import com.yy.exam.modules.sys.user.mapper.SysUserMapper;
import com.yy.exam.modules.sys.user.service.SysUserRoleService;
import com.yy.exam.modules.sys.user.service.SysUserService;
import com.yy.exam.modules.user.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 * 【涉及功能：教师端学生管理】
 * 
 * 职责：处理用户管理的所有业务逻辑，包括：
 * - 用户登录/登出/Token验证
 * - 用户增删改查（含教师端学生管理的角色过滤）
 * - 用户注册（学生自助注册）
 * - Excel批量导入（含角色权限校验）
 * 
 * 教师端学生管理核心逻辑：
 * - 分页查询：管理员看到所有用户，教师只能看到学生
 * - 保存用户：教师只能创建学生角色，管理员可以创建所有角色
 * - 导入用户：教师只能导入学生，管理员可以导入所有角色
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserRoleService sysUserRoleService;


    /**
     * 用户列表分页查询
     * 【新增功能：教师端学生管理】
     * 管理员可以看到所有用户，教师只能看到角色为 student 的用户
     * 
     * 核心逻辑：
     * 1. 接收前端传来的分页参数和搜索条件
     * 2. 判断当前用户角色：如果是管理员，不加过滤条件；如果是教师，加上 role_ids LIKE '%student%'
     * 3. 执行分页查询，返回结果
     * 
     * @param reqDTO 分页请求，包含 current(页码)、size(每页条数)、params(搜索条件)
     * @return 分页结果
     */
    @Override
    public IPage<SysUserDTO> paging(PagingReqDTO<SysUserDTO> reqDTO) {

        // 1. 创建分页对象：第几页、每页多少条
        IPage<SysUser> query = new Page<>(reqDTO.getCurrent(), reqDTO.getSize());

        // 2. 创建查询条件构造器
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();

        // 3. 获取前端传来的搜索参数
        SysUserDTO params = reqDTO.getParams();

        if(params!=null){
            // 按用户名模糊搜索
            if(!StringUtils.isBlank(params.getUserName())){
                wrapper.lambda().like(SysUser::getUserName, params.getUserName());
            }

            // 按真实姓名模糊搜索
            if(!StringUtils.isBlank(params.getRealName())){
                wrapper.lambda().like(SysUser::getRealName, params.getRealName());
            }
        }

        // 4.【新增功能：教师端学生管理】教师只能看到学生
        // UserUtils.isAdmin(false) 判断当前用户是不是管理员
        // false 参数表示：如果不是管理员，不抛异常，返回 false
        // 如果当前用户不是管理员（即教师），则加上过滤条件：只查 role_ids 包含 'student' 的用户
        // 实际SQL：AND role_ids LIKE '%student%'
        if (!UserUtils.isAdmin(false)) {
            wrapper.lambda().like(SysUser::getRoleIds, "student");
        }
        // 注意：如果当前用户是管理员，上面的 if 不会执行，不加过滤条件，能看到所有用户

        // 5. 执行分页查询
        IPage<SysUser> page = this.page(query, wrapper);

        // 6. 把实体类对象转换成DTO对象返回
        IPage<SysUserDTO> pageData = JSON.parseObject(JSON.toJSONString(page), new TypeReference<Page<SysUserDTO>>(){});
        return pageData;
     }

    /**
     * 用户登录
     * 
     * 登录流程：
     * 1. 根据用户名查询用户
     * 2. 检查用户是否存在
     * 3. 检查用户是否被禁用
     * 4. 用盐值+MD5验证密码
     * 5. 生成JWT Token并返回
     * 
     * 密码验证原理：PassHandler.checkPass(明文密码, 盐值, 数据库中的密文)
     * 系统用同样的盐值对明文密码做MD5，然后和数据库中的密文比较
     * 
     * @param userName 用户名
     * @param password 明文密码（前端传过来的）
     * @return 用户信息 + JWT Token
     */
    @Override
    public SysUserLoginDTO login(String userName, String password) {

        // 步骤1：根据用户名查询用户
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, userName);

        SysUser user = this.getOne(wrapper, false);
        // 步骤2：用户不存在
        if(user == null){
            throw new ServiceException(ApiError.ERROR_90010002);
        }

        // 步骤3：用户被禁用（state = ABNORMAL）
        if(user.getState().equals(CommonState.ABNORMAL)){
            throw new ServiceException(ApiError.ERROR_90010005);
        }

        // 步骤4：验证密码（盐值+MD5）
        boolean check = PassHandler.checkPass(password,user.getSalt(), user.getPassword());
        if(!check){
            throw new ServiceException(ApiError.ERROR_90010002);
        }

        // 步骤5：生成Token并返回用户信息
        return this.setToken(user);
    }

    @Override
    public SysUserLoginDTO token(String token) {

        // 获得会话
        String username = JwtUtils.getUsername(token);

        // 校验结果
        boolean check = JwtUtils.verify(token, username);

        if(!check){
            throw new ServiceException(ApiError.ERROR_90010002);
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, username);

        SysUser user = this.getOne(wrapper, false);
        if(user == null){
            throw new ServiceException(ApiError.ERROR_10010002);
        }

        // 被禁用
        if(user.getState().equals(CommonState.ABNORMAL)){
            throw new ServiceException(ApiError.ERROR_90010005);
        }

        return this.setToken(user);
    }

    @Override
    public void logout(String token) {

        // 仅退出当前会话
        SecurityUtils.getSubject().logout();
    }

    @Override
    public void update(SysUserDTO reqDTO) {


       String pass = reqDTO.getPassword();
       if(!StringUtils.isBlank(pass)){
           PassInfo passInfo = PassHandler.buildPassword(pass);
           SysUser user = this.getById(UserUtils.getUserId());
           user.setPassword(passInfo.getPassword());
           user.setSalt(passInfo.getSalt());
           this.updateById(user);
       }
    }

    /**
     * 保存或修改用户
     * 【涉及功能：教师端学生管理 - 核心保存逻辑】
     * 
     * 处理流程：
     * 1. 校验角色列表不能为空
     * 2. 角色权限控制：教师只能创建学生角色的用户，管理员可以创建任意角色
     * 3. 保存用户基本信息（用户名、真实姓名等）
     * 4. 如果是新增，生成雪花ID
     * 5. 如果传了密码，用盐值+MD5加密后保存
     * 6. 保存用户-角色关联关系（先删后插）
     * 7. 保存或更新用户记录
     * 
     * @param reqDTO 用户保存请求（含用户名、密码、角色列表等）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(SysUserSaveReqDTO reqDTO) {

        // 步骤1：获取角色列表
        List<String> roles = reqDTO.getRoles();

        // 角色列表不能为空
        if(CollectionUtils.isEmpty(roles)){
            throw new ServiceException(ApiError.ERROR_90010003);
        }

        // 步骤2：【新增功能：教师端学生管理】教师只能创建学生
        // 如果当前用户不是管理员，强制把角色设为 student
        // 防止教师创建管理员或其他教师账号
        if (!UserUtils.isAdmin(false)) {
            roles = new ArrayList<>();
            roles.add("student");
        }

        // 步骤3：保存基本信息
        SysUser user = new SysUser();
        BeanMapper.copy(reqDTO, user);

        // 步骤4：新增模式，生成雪花ID
        if(StringUtils.isBlank(user.getId())){
            user.setId(IdWorker.getIdStr());
        }

        // 步骤5：如果传了密码，用盐值+MD5加密后保存
        // PassHandler.buildPassword() 随机生成盐值，然后用 盐值+MD5(明文) 生成密文
        if(!StringUtils.isBlank(reqDTO.getPassword())){
            PassInfo pass = PassHandler.buildPassword(reqDTO.getPassword());
            user.setPassword(pass.getPassword());  // 密文
            user.setSalt(pass.getSalt());          // 盐值
        }

        // 步骤6：保存角色信息（先删除旧角色，再插入新角色，保证一致性）
        String roleIds = sysUserRoleService.saveRoles(user.getId(), roles);
        user.setRoleIds(roleIds);  // 角色ID字符串，如 "student" 或 "sa,teacher"

        // 步骤7：保存或更新用户（有ID则更新，无ID则插入）
        this.saveOrUpdate(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysUserLoginDTO reg(SysUserDTO reqDTO) {

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, reqDTO.getUserName());

        int count = this.count(wrapper);

        if(count > 0){
            throw new ServiceException(1, "用户名已存在，换一个吧！");
        }


        // 保存用户
        SysUser user = new SysUser();
        user.setId(IdWorker.getIdStr());
        user.setUserName(reqDTO.getUserName());
        user.setRealName(reqDTO.getRealName());
        PassInfo passInfo = PassHandler.buildPassword(reqDTO.getPassword());
        user.setPassword(passInfo.getPassword());
        user.setSalt(passInfo.getSalt());

        // 保存角色
        List<String> roles = reqDTO.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            roles = new ArrayList<>();
            roles.add("student");
        }
        String roleIds = sysUserRoleService.saveRoles(user.getId(), roles);
        user.setRoleIds(roleIds);
        this.save(user);

        return this.setToken(user);
    }

    @Override
    public SysUserLoginDTO quickReg(SysUserDTO reqDTO) {

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, reqDTO.getUserName());
        wrapper.last(" LIMIT 1 ");
        SysUser user = this.getOne(wrapper);
        if(user!=null){
            return this.setToken(user);
        }

        return this.reg(reqDTO);
    }


    /**
     * 批量导入用户（Excel导入）
     * 【涉及功能：教师端学生管理 - Excel批量导入】
     * 
     * 导入流程：
     * 1. 校验导入数据不能为空
     * 2. 判断当前用户角色，确定允许导入的角色类型
     *    - 管理员：可以导入 sa、teacher、student
     *    - 教师：只能导入 student
     * 3. 逐行处理Excel数据：
     *    a. 校验必填字段（用户名、真实姓名、密码）
     *    b. 校验角色权限（教师不能导入管理员）
     *    c. 检查用户名是否已存在（已存在则跳过）
     *    d. 用盐值+MD5加密密码
     *    e. 保存用户和角色关联
     * 4. 统计导入结果，返回成功/失败数量
     * 
     * 事务管理：@Transactional 保证所有操作要么全部成功，要么全部回滚
     * 
     * @param list Excel解析后的数据列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(List<SysUserImportDTO> list) {

        // 步骤1：校验数据不能为空
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(1, "导入数据为空！");
        }

        // 步骤2：【新增功能：教师端学生管理】权限控制
        // 管理员可以导入所有角色，教师只能导入学生
        boolean isAdmin = UserUtils.isAdmin(false);
        List<String> allowedRoles = new ArrayList<>();
        allowedRoles.add("student");           // 所有人都可以导入学生
        if (isAdmin) {
            allowedRoles.add("teacher");       // 只有管理员可以导入教师
            allowedRoles.add("sa");            // 只有管理员可以导入管理员
        }

        // 统计变量
        int success = 0;                       // 成功导入数量
        int fail = 0;                          // 失败/跳过数量
        StringBuffer errors = new StringBuffer();  // 错误信息收集

        // 步骤3：逐行处理
        for (SysUserImportDTO item : list) {
            try {
                // 校验必填字段
                if (StringUtils.isBlank(item.getUserName())) {
                    fail++;
                    continue;
                }
                if (StringUtils.isBlank(item.getRealName())) {
                    fail++;
                    continue;
                }
                if (StringUtils.isBlank(item.getPassword())) {
                    fail++;
                    continue;
                }

                // 校验角色：默认为 student，教师不能导入非student角色
                String role = item.getRole();
                if (StringUtils.isBlank(role)) {
                    role = "student";
                }
                if (!allowedRoles.contains(role)) {
                    errors.append("用户" + item.getUserName() + "的角色不允许导入，已跳过<br>");
                    fail++;
                    continue;
                }

                // 检查用户名是否已存在（已存在则跳过，不覆盖）
                QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
                wrapper.lambda().eq(SysUser::getUserName, item.getUserName());
                int count = this.count(wrapper);
                if (count > 0) {
                    errors.append("用户" + item.getUserName() + "已存在，已跳过<br>");
                    fail++;
                    continue;
                }

                // 保存用户
                SysUser user = new SysUser();
                user.setId(IdWorker.getIdStr());
                user.setUserName(item.getUserName());
                user.setRealName(item.getRealName());

                // 密码加密（盐值+MD5）
                PassInfo passInfo = PassHandler.buildPassword(item.getPassword());
                user.setPassword(passInfo.getPassword());
                user.setSalt(passInfo.getSalt());

                // 保存角色关联
                List<String> roles = new ArrayList<>();
                roles.add(role);
                String roleIds = sysUserRoleService.saveRoles(user.getId(), roles);
                user.setRoleIds(roleIds);

                // 保存用户到数据库
                this.save(user);

                success++;
            } catch (Exception e) {
                fail++;
                errors.append("导入" + item.getUserName() + "失败：" + e.getMessage() + "<br>");
            }
        }

        // 步骤4：组装导入结果消息
        String msg = "成功导入" + success + "条";
        if (fail > 0) {
            msg += "，跳过" + fail + "条";
            if (errors.length() > 0) {
                msg += "。" + errors.toString();
            }
        }

        // 如果有失败记录，抛出异常提示用户
        if (fail > 0 && errors.length() > 0) {
            throw new ServiceException(1, msg);
        }
    }


    /**
     * 保存会话信息（生成Token并返回用户信息）
     * 
     * 登录成功后调用此方法，完成以下操作：
     * 1. 把用户基本信息拷贝到响应DTO（用户名、真实姓名、角色等）
     * 2. 用JWT工具生成Token（Token中包含用户名，有效期在配置文件中定义）
     * 3. 查询用户的所有角色列表，填充到响应DTO中
     * 
     * JWT Token 结构：
     * Header（算法类型）+ Payload（用户名、过期时间）+ Signature（签名）
     * 
     * 前端收到Token后存在localStorage，后续每次请求都带在请求头中
     * 
     * @param user 数据库中的用户实体
     * @return 包含用户信息和Token的登录响应DTO
     */
    private SysUserLoginDTO setToken(SysUser user){

        SysUserLoginDTO respDTO = new SysUserLoginDTO();
        BeanMapper.copy(user, respDTO);

        // 步骤1：生成JWT Token
        // JwtUtils.sign() 用用户名+密钥+过期时间生成Token
        String token = JwtUtils.sign(user.getUserName());
        respDTO.setToken(token);

        // 步骤2：填充角色列表
        // 从 sys_user_role 表查询该用户的所有角色
        List<String> roles = sysUserRoleService.listRoles(user.getId());
        respDTO.setRoles(roles);

        return respDTO;
    }
}