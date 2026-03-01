package com.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("teacher_course_class")
public class TeacherCourseClass {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private Long courseId;

    private Long classId;

    private Date createTime;
}
