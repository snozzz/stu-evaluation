package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("semester")
public class Semester {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Date startDate;

    private Date endDate;

    private Integer isCurrent;

    private Date createTime;
}
