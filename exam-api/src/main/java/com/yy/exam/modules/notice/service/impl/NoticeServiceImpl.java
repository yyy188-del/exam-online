package com.yy.exam.modules.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.exam.core.api.dto.PagingReqDTO;
import com.yy.exam.core.utils.BeanMapper;
import com.yy.exam.modules.notice.dto.NoticeDTO;
import com.yy.exam.modules.notice.entity.Notice;
import com.yy.exam.modules.notice.mapper.NoticeMapper;
import com.yy.exam.modules.notice.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public IPage<NoticeDTO> paging(PagingReqDTO<NoticeDTO> reqDTO) {
        Page<Notice> page = new Page<>(reqDTO.getCurrent(), reqDTO.getSize());
        QueryWrapper<Notice> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");

        IPage<Notice> result = this.page(page, wrapper);
        return result.convert(item -> {
            NoticeDTO dto = new NoticeDTO();
            BeanMapper.copy(item, dto);
            return dto;
        });
    }

    @Override
    public void save(NoticeDTO reqDTO) {
        Notice entity = new Notice();
        BeanMapper.copy(reqDTO, entity);
        this.saveOrUpdate(entity);
    }
}