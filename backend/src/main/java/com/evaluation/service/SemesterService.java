package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.Semester;

public interface SemesterService extends IService<Semester> {

    Semester getCurrentSemester();
}
