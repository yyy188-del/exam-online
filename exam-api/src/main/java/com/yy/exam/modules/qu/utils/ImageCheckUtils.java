package com.yy.exam.modules.qu.utils;

import com.yy.exam.ability.upload.config.UploadConfig;
import com.yy.exam.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImageCheckUtils {

    @Autowired
    private UploadConfig conf;

    /**
     * 进行图片校验！
     * @param image
     * @param throwMsg
     */
    public void checkImage(String image, String throwMsg) {

        if(StringUtils.isBlank(image)){
            return;
        }

        // 校验图片地址
        if(!image.startsWith(conf.getUrl())){
            throw new ServiceException(throwMsg);
        }
    }
}
