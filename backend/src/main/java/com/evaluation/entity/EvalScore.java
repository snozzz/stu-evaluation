package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("eval_score")
public class EvalScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long courseId;

    private Long dimensionId;

    private Long teacherId;

    private BigDecimal score;

    private Date evalDate;

    private Long semesterId;

    private Date createTime;

    private Date updateTime;
}
