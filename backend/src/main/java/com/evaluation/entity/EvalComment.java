package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("eval_comment")
public class EvalComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    @TableField(exist = false)
    private String studentName;

    private Long courseId;

    private Long teacherId;

    private String comment;

    private String aiComment;

    private Long semesterId;

    private Integer isPublished;

    private Date createTime;

    private Date updateTime;
}
