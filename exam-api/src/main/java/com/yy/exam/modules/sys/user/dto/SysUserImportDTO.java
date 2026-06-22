package com.yy.exam.modules.sys.user.dto;

import com.yy.exam.core.utils.excel.annotation.ExcelField;
import lombok.Data;

@Data
public class SysUserImportDTO {

    @ExcelField(title = "用户名", align = 2, sort = 1)
    private String userName;

    @ExcelField(title = "真实姓名", align = 2, sort = 2)
    private String realName;

    @ExcelField(title = "密码", align = 2, sort = 3)
    private String password;

    @ExcelField(title = "角色", align = 2, sort = 4)
    private String role;
}