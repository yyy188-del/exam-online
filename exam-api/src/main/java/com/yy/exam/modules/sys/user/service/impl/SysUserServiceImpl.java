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
* <p>
* 语言设置 服务实现类
* </p>
*
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserRoleService sysUserRoleService;


    @Override
    public IPage<SysUserDTO> paging(PagingReqDTO<SysUserDTO> reqDTO) {

        //创建分页对象
        IPage<SysUser> query = new Page<>(reqDTO.getCurrent(), reqDTO.getSize());

        //查询条件
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();

        SysUserDTO params = reqDTO.getParams();

        if(params!=null){
            if(!StringUtils.isBlank(params.getUserName())){
                wrapper.lambda().like(SysUser::getUserName, params.getUserName());
            }

            if(!StringUtils.isBlank(params.getRealName())){
                wrapper.lambda().like(SysUser::getRealName, params.getRealName());
            }
        }

        //获得数据
        IPage<SysUser> page = this.page(query, wrapper);
        //转换结果
        IPage<SysUserDTO> pageData = JSON.parseObject(JSON.toJSONString(page), new TypeReference<Page<SysUserDTO>>(){});
        return pageData;
     }

    @Override
    public SysUserLoginDTO login(String userName, String password) {

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUser::getUserName, userName);

        SysUser user = this.getOne(wrapper, false);
        if(user == null){
            throw new ServiceException(ApiError.ERROR_90010002);
        }

        // 被禁用
        if(user.getState().equals(CommonState.ABNORMAL)){
            throw new ServiceException(ApiError.ERROR_90010005);
        }

        boolean check = PassHandler.checkPass(password,user.getSalt(), user.getPassword());
        if(!check){
            throw new ServiceException(ApiError.ERROR_90010002);
        }

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(SysUserSaveReqDTO reqDTO) {

        List<String> roles = reqDTO.getRoles();

        if(CollectionUtils.isEmpty(roles)){
            throw new ServiceException(ApiError.ERROR_90010003);
        }

        // 保存基本信息
        SysUser user = new SysUser();
        BeanMapper.copy(reqDTO, user);

        // 添加模式
        if(StringUtils.isBlank(user.getId())){
            user.setId(IdWorker.getIdStr());
        }

        // 修改密码
        if(!StringUtils.isBlank(reqDTO.getPassword())){
            PassInfo pass = PassHandler.buildPassword(reqDTO.getPassword());
            user.setPassword(pass.getPassword());
            user.setSalt(pass.getSalt());
        }

        // 保存角色信息
        String roleIds = sysUserRoleService.saveRoles(user.getId(), roles);
        user.setRoleIds(roleIds);
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
        List<String> roles = new ArrayList<>();
        roles.add("student");
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


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(List<SysUserImportDTO> list) {

        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(1, "导入数据为空！");
        }

        // 判断当前用户角色，非超管只能导入学生
        boolean isAdmin = UserUtils.isAdmin(false);
        List<String> allowedRoles = new ArrayList<>();
        allowedRoles.add("student");
        if (isAdmin) {
            allowedRoles.add("teacher");
            allowedRoles.add("sa");
        }

        int success = 0;
        int fail = 0;
        StringBuffer errors = new StringBuffer();

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

                // 校验角色
                String role = item.getRole();
                if (StringUtils.isBlank(role)) {
                    role = "student";
                }
                if (!allowedRoles.contains(role)) {
                    errors.append("用户" + item.getUserName() + "的角色不允许导入，已跳过<br>");
                    fail++;
                    continue;
                }

                // 检查用户名是否已存在
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
                PassInfo passInfo = PassHandler.buildPassword(item.getPassword());
                user.setPassword(passInfo.getPassword());
                user.setSalt(passInfo.getSalt());

                // 保存角色
                List<String> roles = new ArrayList<>();
                roles.add(role);
                String roleIds = sysUserRoleService.saveRoles(user.getId(), roles);
                user.setRoleIds(roleIds);
                this.save(user);

                success++;
            } catch (Exception e) {
                fail++;
                errors.append("导入" + item.getUserName() + "失败：" + e.getMessage() + "<br>");
            }
        }

        String msg = "成功导入" + success + "条";
        if (fail > 0) {
            msg += "，跳过" + fail + "条";
            if (errors.length() > 0) {
                msg += "。" + errors.toString();
            }
        }

        // 有错误信息时提示
        if (fail > 0 && errors.length() > 0) {
            throw new ServiceException(1, msg);
        }
    }


    /**
     * 保存会话信息
     * @param user
     * @return
     */
    private SysUserLoginDTO setToken(SysUser user){

        SysUserLoginDTO respDTO = new SysUserLoginDTO();
        BeanMapper.copy(user, respDTO);

        // 生成Token
        String token = JwtUtils.sign(user.getUserName());
        respDTO.setToken(token);

        // 填充角色
        List<String> roles = sysUserRoleService.listRoles(user.getId());
        respDTO.setRoles(roles);

        return respDTO;
    }
}