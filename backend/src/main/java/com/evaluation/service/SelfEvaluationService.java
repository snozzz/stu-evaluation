package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.SelfEvaluation;

import java.util.List;

public interface SelfEvaluationService extends IService<SelfEvaluation> {

    List<SelfEvaluation> getByStudentAndCourse(Long studentId, Long courseId);

    boolean saveOrUpdateByBizKey(SelfEvaluation selfEvaluation);

    boolean saveOrUpdateBatchByBizKey(List<SelfEvaluation> selfEvaluations);
}
