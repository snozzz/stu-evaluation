package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("class_info")
public class ClassInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String college;

    private String major;

    private String grade;

    private Date createTime;
}
