package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("appeal")
public class Appeal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long courseId;

    private Long teacherId;

    private String content;

    private String reply;

    private String status;

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String courseName;
}
