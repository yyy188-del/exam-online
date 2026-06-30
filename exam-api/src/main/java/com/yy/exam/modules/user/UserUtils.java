package com.yy.exam.modules.user;

import com.yy.exam.core.api.ApiError;
import com.yy.exam.core.exception.ServiceException;
import com.yy.exam.modules.sys.user.dto.response.SysUserLoginDTO;
import org.apache.shiro.SecurityUtils;

/**
 * 用户静态工具类
 * 提供从 Shiro 会话中获取当前登录用户信息的方法
 * 以及判断当前用户角色的快捷方法
 * 
 * 核心原理：
 * 用户登录后，Shiro 会把用户信息存储在 SecurityUtils.getSubject().getPrincipal() 中
 * 后续任何地方都可以通过 SecurityUtils 拿到当前用户信息，无需再查数据库
 * 
 * @author bool
 */
public class UserUtils {


    /**
     * 获取当前登录用户的ID
     * @param throwable 如果为 true 且用户未登录，则抛出异常；如果为 false 则返回 null
     * @return 用户ID 或 null
     */
    public static String getUserId(boolean throwable){
        try {
            // 从 Shiro 的会话中取出当前登录用户信息，然后获取用户ID
            return ((SysUserLoginDTO) SecurityUtils.getSubject().getPrincipal()).getId();
        }catch (Exception e){
            if(throwable){
                throw new ServiceException(ApiError.ERROR_10010002);
            }
            return null;
        }
    }

    /**
     * 【新增功能：教师角色判断】
     * 判断当前登录用户是否是超级管理员（sa）
     * 原理：从 Shiro 会话中取出用户的角色列表，检查是否包含 "sa"
     * 
     * 使用场景：
     * - 教师端学生管理：管理员不限制，能看到所有用户；教师只能看到学生
     * - 仪表盘：管理员看到全量数据，教师看到自己的数据
     * 
     * @param throwable 如果为 true 且用户未登录，则抛出异常
     * @return true=是管理员，false=不是管理员
     */
    public static boolean isAdmin(boolean throwable){
        try {
            // 从 Shiro 会话中拿到当前登录用户信息
            SysUserLoginDTO dto = ((SysUserLoginDTO) SecurityUtils.getSubject().getPrincipal());
            // 判断用户的角色列表里是否包含 "sa"（超级管理员角色）
            return dto.getRoles().contains("sa");
        }catch (Exception e){
            if(throwable){
                throw new ServiceException(ApiError.ERROR_10010002);
            }
        }

        return false;
    }

    /**
     * 获取当前登录用户的ID，默认会抛异常（如果未登录）
     * @return 用户ID
     */
    public static String getUserId(){
        return getUserId(true);
    }

    /**
     * 【新增功能：教师角色判断】
     * 判断当前登录用户是否是教师（teacher）
     * 原理：从 Shiro 会话中取出用户的角色列表，检查是否包含 "teacher"
     * 
     * 使用场景：
     * - 考试管理：教师可以创建/编辑/删除考试
     * - 试题管理：教师可以管理题库和试题
     * - 学生管理：教师可以查看学生列表
     * 
     * @param throwable 如果为 true 且用户未登录，则抛出异常
     * @return true=是教师，false=不是教师
     */
    public static boolean isTeacher(boolean throwable){
        try {
            // 从 Shiro 会话中取出用户信息
            SysUserLoginDTO dto = ((SysUserLoginDTO) SecurityUtils.getSubject().getPrincipal());
            // 判断角色列表里是否包含 "teacher"
            return dto.getRoles().contains("teacher");
        }catch (Exception e){
            if(throwable){
                throw new ServiceException(ApiError.ERROR_10010002);
            }
        }
        return false;
    }
}