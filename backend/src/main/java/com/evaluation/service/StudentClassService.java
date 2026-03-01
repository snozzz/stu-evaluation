package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.StudentClass;

import java.util.List;

public interface StudentClassService extends IService<StudentClass> {

    List<StudentClass> getByStudentId(Long studentId);
}
