package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("eval_dimension")
public class EvalDimension {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Integer sortOrder;

    private Date createTime;
}
