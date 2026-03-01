package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("course_eval_weight")
public class CourseEvalWeight {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long dimensionId;

    private BigDecimal weight;
}
