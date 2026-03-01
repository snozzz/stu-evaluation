package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("alert_rule")
public class AlertRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String ruleType;

    private BigDecimal threshold;

    private Integer isEnabled;

    private Date createTime;
}
