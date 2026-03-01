package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.EvalScore;

import java.math.BigDecimal;
import java.util.List;

public interface EvalScoreService extends IService<EvalScore> {

    List<EvalScore> getStudentScores(Long studentId, Long courseId);

    List<EvalScore> getClassScores(Long courseId, Long classId);

    BigDecimal calculateWeightedScore(Long studentId, Long courseId);
}
