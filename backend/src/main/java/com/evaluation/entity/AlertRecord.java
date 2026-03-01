package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("alert_record")
public class AlertRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ruleId;

    private Long userId;

    private Long courseId;

    private String message;

    private Integer isRead;

    private Date createTime;
}
