package com.yy.exam.modules.repo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yy.exam.core.api.ApiRest;
import com.yy.exam.core.api.controller.BaseController;
import com.yy.exam.core.api.dto.BaseIdReqDTO;
import com.yy.exam.core.api.dto.BaseIdsReqDTO;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.modules.qu.dto.request.QuRepoBatchReqDTO;
import com.yy.exam.modules.qu.service.QuRepoService;
import com.yy.exam.modules.repo.dto.RepoDTO;
import com.yy.exam.modules.repo.dto.request.RepoReqDTO;
import com.yy.exam.modules.repo.dto.response.RepoRespDTO;
import com.yy.exam.modules.repo.entity.Repo;
import com.yy.exam.modules.repo.service.RepoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
* <p>
* 题库控制器
* </p>
*
*/
@Api(tags={"题库"})
@RestController
@RequestMapping("/exam/api/repo")
public class RepoController extends BaseController {

    @Autowired
    private RepoService baseService;

    @Autowired
    private QuRepoService quRepoService;

    /**
    * 添加或修改
    * @param reqDTO
    * @return
    */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)    @ApiOperation(value = "添加或修改")
    @RequestMapping(value = "/save", method = { RequestMethod.POST})
    public ApiRest save(@RequestBody RepoDTO reqDTO) {
        baseService.save(reqDTO);
        return super.success();
    }

    /**
    * 批量删除
    * @param reqDTO
    * @return
    */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/delete", method = { RequestMethod.POST})
    public ApiRest edit(@RequestBody BaseIdsReqDTO reqDTO) {
        //根据ID删除
        baseService.removeByIds(reqDTO.getIds());
        return super.success();
    }

    /**
    * 查找详情
    * @param reqDTO
    * @return
    */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)    @ApiOperation(value = "查找详情")
    @RequestMapping(value = "/detail", method = { RequestMethod.POST})
    public ApiRest<RepoDTO> find(@RequestBody BaseIdReqDTO reqDTO) {
        Repo entity = baseService.getById(reqDTO.getId());
        RepoDTO dto = new RepoDTO();
        BeanUtils.copyProperties(entity, dto);
        return super.success(dto);
    }

    /**
    * 分页查找
    * @param reqDTO
    * @return
    */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)    @ApiOperation(value = "分页查找")
    @RequestMapping(value = "/paging", method = { RequestMethod.POST})
    public ApiRest<IPage<RepoRespDTO>> paging(@RequestBody PagingReqDTO<RepoReqDTO> reqDTO) {

        //分页查询并转换
        IPage<RepoRespDTO> page = baseService.paging(reqDTO);

        return super.success(page);
    }

    /**
     * 批量操作
     * @param reqDTO
     * @return
     */
    @RequiresRoles(value = {"sa", "teacher"}, logical = Logical.OR)    @ApiOperation(value = "批量操作", notes = "批量加入或从题库移除")
    @RequestMapping(value = "/batch-action", method = { RequestMethod.POST})
    public ApiRest batchAction(@RequestBody QuRepoBatchReqDTO reqDTO) {

        //分页查询并转换
        quRepoService.batchAction(reqDTO);
        return super.success();
    }
}
