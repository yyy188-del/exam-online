package com.yy.exam.modules.notice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yy.exam.core.api.ApiRest;
import com.yy.exam.core.api.controller.BaseController;
import com.yy.exam.core.api.dto.BaseIdReqDTO;
import com.yy.exam.core.api.dto.BaseIdsReqDTO;
import com.yy.exam.core.api.dto.BaseStateReqDTO;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.modules.notice.dto.NoticeDTO;
import com.yy.exam.modules.notice.entity.Notice;
import com.yy.exam.modules.notice.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Api(tags={"考试公告"})
@RestController
@RequestMapping("/exam/api/notice")
public class NoticeController extends BaseController {

    @Autowired
    private NoticeService baseService;

    @RequiresRoles("sa")
    @ApiOperation(value = "添加或修改")
    @RequestMapping(value = "/save", method = { RequestMethod.POST})
    public ApiRest save(@RequestBody NoticeDTO reqDTO) {
        baseService.save(reqDTO);
        return super.success();
    }

    @RequiresRoles("sa")
    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/delete", method = { RequestMethod.POST})
    public ApiRest delete(@RequestBody BaseIdsReqDTO reqDTO) {
        baseService.removeByIds(reqDTO.getIds());
        return super.success();
    }

    @ApiOperation(value = "查找详情")
    @RequestMapping(value = "/detail", method = { RequestMethod.POST})
    public ApiRest<NoticeDTO> detail(@RequestBody BaseIdReqDTO reqDTO) {
        Notice entity = baseService.getById(reqDTO.getId());
        NoticeDTO dto = new NoticeDTO();
        org.springframework.beans.BeanUtils.copyProperties(entity, dto);
        return super.success(dto);
    }

    @RequiresRoles("sa")
    @ApiOperation(value = "修改状态")
    @RequestMapping(value = "/state", method = { RequestMethod.POST})
    public ApiRest state(@RequestBody BaseStateReqDTO reqDTO) {
        for (String id : reqDTO.getIds()) {
            Notice notice = new Notice();
            notice.setId(id);
            notice.setState(reqDTO.getState());
            notice.setUpdateTime(new Date());
            baseService.updateById(notice);
        }
        return super.success();
    }

    @ApiOperation(value = "分页查找")
    @RequestMapping(value = "/paging", method = { RequestMethod.POST})
    public ApiRest<IPage<NoticeDTO>> paging(@RequestBody PagingReqDTO<NoticeDTO> reqDTO) {
        IPage<NoticeDTO> page = baseService.paging(reqDTO);
        return super.success(page);
    }
}