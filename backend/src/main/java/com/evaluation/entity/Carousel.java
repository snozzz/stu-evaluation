package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("carousel")
public class Carousel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private Integer sortOrder;

    private Integer status;

    private Date createTime;
}
