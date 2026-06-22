package com.yy.exam.modules.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.modules.notice.dto.NoticeDTO;
import com.yy.exam.modules.notice.entity.Notice;

public interface NoticeService extends IService<Notice> {

    IPage<NoticeDTO> paging(PagingReqDTO<NoticeDTO> reqDTO);

    void save(NoticeDTO reqDTO);
}