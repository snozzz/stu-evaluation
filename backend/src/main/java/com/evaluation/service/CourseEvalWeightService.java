package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.CourseEvalWeight;

import java.util.List;

public interface CourseEvalWeightService extends IService<CourseEvalWeight> {

    List<CourseEvalWeight> getByCourseId(Long courseId);
}
