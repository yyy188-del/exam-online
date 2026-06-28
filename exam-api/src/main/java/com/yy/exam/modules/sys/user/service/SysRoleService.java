package com.yy.exam.modules.sys.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yy.exam.modules.sys.user.dto.SysRoleDTO;
import com.yy.exam.modules.sys.user.entity.SysRole;
import com.yy.exam.core.api.dto.PagingReqDTO;

/**
* <p>
* 角色业务类
* </p>
*
*/
public interface SysRoleService extends IService<SysRole> {

    /**
    * 分页查询数据
    * @param reqDTO
    * @return
    */
    IPage<SysRoleDTO> paging(PagingReqDTO<SysRoleDTO> reqDTO);
}
