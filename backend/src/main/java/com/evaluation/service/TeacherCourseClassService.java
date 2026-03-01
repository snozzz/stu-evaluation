package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.TeacherCourseClass;

import java.util.List;

public interface TeacherCourseClassService extends IService<TeacherCourseClass> {

    List<TeacherCourseClass> getByTeacherId(Long teacherId);

    List<Long> getCoursesByTeacher(Long teacherId);
}
