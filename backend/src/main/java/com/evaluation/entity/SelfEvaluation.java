package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("self_evaluation")
public class SelfEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long courseId;

    private Long dimensionId;

    private BigDecimal score;

    private String comment;

    private Long semesterId;

    private Date createTime;

    private Date updateTime;
}
