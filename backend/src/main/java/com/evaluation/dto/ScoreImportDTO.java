package com.evaluation.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ScoreImportDTO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("考勤")
    private Double attendance;

    @ExcelProperty("作业")
    private Double homework;

    @ExcelProperty("实验")
    private Double experiment;

    @ExcelProperty("测验")
    private Double quiz;

    @ExcelProperty("课堂参与")
    private Double participation;
}
