package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("assignment_submission")
public class AssignmentSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assignmentId;

    private Long studentId;

    private String fileUrl;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date submitTime;

    private String status;

    private BigDecimal score;

    private String feedback;
}
