package com.yy.exam.modules.sys.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yy.exam.modules.sys.user.dto.SysUserRoleDTO;
import com.yy.exam.modules.sys.user.entity.SysUserRole;
import com.yy.exam.core.api.dto.PagingReqDTO;

import java.util.List;

/**
 * 用户角色业务接口
 * 管理系统中的用户-角色关联关系
 * 三种角色：sa（超级管理员）、teacher（教师）、student（学生）
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    /**
    * 分页查询数据
    * @param reqDTO
    * @return
    */
    IPage<SysUserRoleDTO> paging(PagingReqDTO<SysUserRoleDTO> reqDTO);

    /**
     * 查找用户的所有角色列表
     * @param userId 用户ID
     * @return 角色ID列表，如 ["sa", "teacher"]
     */
    List<String> listRoles(String userId);

    /**
     * 保存用户角色（先删后插，保证原子性）
     * @param userId 用户ID
     * @param ids 角色ID列表
     * @return 角色ID拼接字符串，如 "sa,teacher"
     */
    String saveRoles(String userId, List<String> ids);

    /**
     * 【新增功能：角色判断】
     * 判断指定用户是否拥有学生角色
     * 原理：查 sys_user_role 表，看是否有 user_id=? AND role_id='student' 的记录
     * @param userId 用户ID
     * @return true=是学生，false=不是学生
     */
    boolean isStudent(String userId);

    /**
     * 【新增功能：教师角色判断】
     * 判断指定用户是否拥有教师角色
     * 原理：查 sys_user_role 表，看是否有 user_id=? AND role_id='teacher' 的记录
     * 
     * 使用场景：
     * - 仪表盘统计：根据角色返回不同的统计数据
     * - 权限控制：教师可以访问考试管理、试题管理等模块
     * 
     * @param userId 用户ID
     * @return true=是教师，false=不是教师
     */
    boolean isTeacher(String userId);

    /**
     * 【新增功能：管理员角色判断】
     * 判断指定用户是否拥有超级管理员角色
     * 原理：查 sys_user_role 表，看是否有 user_id=? AND role_id='sa' 的记录
     * 
     * 使用场景：
     * - 仪表盘统计：管理员看到全量数据
     * - 教师端学生管理：管理员不受限制，能看到所有用户
     * 
     * @param userId 用户ID
     * @return true=是管理员，false=不是管理员
     */
    boolean isAdmin(String userId);
}