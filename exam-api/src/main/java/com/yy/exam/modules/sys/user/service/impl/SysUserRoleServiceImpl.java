package com.yy.exam.modules.sys.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.exam.modules.sys.user.dto.SysUserRoleDTO;
import com.yy.exam.modules.sys.user.entity.SysUserRole;
import com.yy.exam.modules.sys.user.mapper.SysUserRoleMapper;
import com.yy.exam.modules.sys.user.service.SysUserRoleService;
import com.yy.exam.core.api.dto.PagingReqDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色服务实现类
 * 负责用户-角色关联关系的增删改查
 * 以及用户角色判断（isStudent、isTeacher、isAdmin）
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Override
    public IPage<SysUserRoleDTO> paging(PagingReqDTO<SysUserRoleDTO> reqDTO) {

        //创建分页对象
        IPage<SysUserRole> query = new Page<>(reqDTO.getCurrent(), reqDTO.getSize());

        //查询条件
        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();

        //获得数据
        IPage<SysUserRole> page = this.page(query, wrapper);
        //转换结果
        IPage<SysUserRoleDTO> pageData = JSON.parseObject(JSON.toJSONString(page), new TypeReference<Page<SysUserRoleDTO>>(){});
        return pageData;
     }

    @Override
    public List<String> listRoles(String userId) {

        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUserRole::getUserId, userId);

        List<SysUserRole> list = this.list(wrapper);
        List<String> roles = new ArrayList<>();
        if(!CollectionUtils.isEmpty(list)){
            for(SysUserRole item: list){
                roles.add(item.getRoleId());
            }
        }

        return roles;
    }

    @Override
    public String saveRoles(String userId, List<String> ids) {

        // 删除全部角色
        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUserRole::getUserId, userId);
        this.remove(wrapper);


        if(!CollectionUtils.isEmpty(ids)){

            List<SysUserRole> list = new ArrayList<>();
            String roleIds = null;

            for(String item: ids){
                SysUserRole role = new SysUserRole();
                role.setRoleId(item);
                role.setUserId(userId);
                list.add(role);
                if(StringUtils.isEmpty(roleIds)){
                    roleIds = item;
                }else{
                    roleIds+=","+item;
                }
            }

            this.saveBatch(list);
            return roleIds;
        }

        return "";
    }

    /**
     * 【新增功能：学生角色判断】
     * 判断指定用户是否是学生
     * 原理：查询 sys_user_role 表，看是否存在 user_id=? AND role_id='student' 的记录
     * 实际执行的SQL：SELECT COUNT(*) FROM sys_user_role WHERE user_id=? AND role_id='student'
     * 
     * @param userId 用户ID
     * @return true=是学生，false=不是学生
     */
    @Override
    public boolean isStudent(String userId) {

        // 构造查询条件：user_id = ? AND role_id = 'student'
        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, "student");

        // 如果查询到的记录数 > 0，说明该用户拥有学生角色
        return this.count(wrapper) > 0;
    }

    /**
     * 【新增功能：教师角色判断】
     * 判断指定用户是否是教师
     * 原理：查询 sys_user_role 表，看是否存在 user_id=? AND role_id='teacher' 的记录
     * 实际执行的SQL：SELECT COUNT(*) FROM sys_user_role WHERE user_id=? AND role_id='teacher'
     * 
     * 这个方法是仪表盘、权限控制等模块的核心依赖
     * 
     * @param userId 用户ID
     * @return true=是教师，false=不是教师
     */
    @Override
    public boolean isTeacher(String userId) {
        // 构造查询条件：user_id = ? AND role_id = 'teacher'
        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, "teacher");

        // 如果查询到的记录数 > 0，说明该用户拥有教师角色
        return this.count(wrapper) > 0;
    }

    /**
     * 【新增功能：管理员角色判断】
     * 判断指定用户是否是超级管理员
     * 原理：查询 sys_user_role 表，看是否存在 user_id=? AND role_id='sa' 的记录
     * 实际执行的SQL：SELECT COUNT(*) FROM sys_user_role WHERE user_id=? AND role_id='sa'
     * 
     * @param userId 用户ID
     * @return true=是管理员，false=不是管理员
     */
    @Override
    public boolean isAdmin(String userId) {
        // 构造查询条件：user_id = ? AND role_id = 'sa'
        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, "sa");

        // 如果查询到的记录数 > 0，说明该用户拥有管理员角色
        return this.count(wrapper) > 0;
    }
}