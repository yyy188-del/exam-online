package com.yy.exam.modules.paper.dto.export;

import com.yy.exam.core.utils.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Date;

/**
 * 考试成绩导出DTO
 * @author 你的名字
 */
@Data
public class ExamScoreExportDTO {

    @ExcelField(title = "序号", align = 2, sort = 1)
    private Integer rowNum;

    @ExcelField(title = "考生姓名", align = 1, sort = 2)
    private String realName;

    @ExcelField(title = "用户名", align = 1, sort = 3)
    private String userName;

    @ExcelField(title = "考试名称", align = 1, sort = 4)
    private String examTitle;

    @ExcelField(title = "总分", align = 1, sort = 5)
    private Integer totalScore;

    @ExcelField(title = "得分", align = 1, sort = 6)
    private Integer userScore;

    @ExcelField(title = "正确率", align = 1, sort = 7)
    private String accuracy;

    @ExcelField(title = "是否及格", align = 1, sort = 8)
    private String passed;

    @ExcelField(title = "用时(分钟)", align = 1, sort = 9)
    private Integer userTime;

    @ExcelField(title = "交卷时间", align = 1, sort = 10)
    private Date updateTime;
}