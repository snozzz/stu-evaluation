package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.CourseEvalWeight;
import com.evaluation.entity.EvalScore;
import com.evaluation.entity.StudentClass;
import com.evaluation.mapper.CourseEvalWeightMapper;
import com.evaluation.mapper.EvalScoreMapper;
import com.evaluation.mapper.StudentClassMapper;
import com.evaluation.service.EvalScoreService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvalScoreServiceImpl extends ServiceImpl<EvalScoreMapper, EvalScore> implements EvalScoreService {

    @Resource
    private EvalScoreMapper evalScoreMapper;

    @Resource
    private CourseEvalWeightMapper courseEvalWeightMapper;

    @Resource
    private StudentClassMapper studentClassMapper;

    @Override
    public List<EvalScore> getStudentScores(Long studentId, Long courseId) {
        QueryWrapper<EvalScore> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        queryWrapper.eq("course_id", courseId);
        return evalScoreMapper.selectList(queryWrapper);
    }

    @Override
    public List<EvalScore> getClassScores(Long courseId, Long classId) {
        // Get all student IDs in the class
        QueryWrapper<StudentClass> scWrapper = new QueryWrapper<>();
        scWrapper.eq("class_id", classId);
        List<StudentClass> studentClasses = studentClassMapper.selectList(scWrapper);
        List<Long> studentIds = studentClasses.stream()
                .map(StudentClass::getStudentId)
                .collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // Get all scores for these students in the course
        QueryWrapper<EvalScore> scoreWrapper = new QueryWrapper<>();
        scoreWrapper.eq("course_id", courseId);
        scoreWrapper.in("student_id", studentIds);
        return evalScoreMapper.selectList(scoreWrapper);
    }

    @Override
    public BigDecimal calculateWeightedScore(Long studentId, Long courseId) {
        // Get all scores for this student in this course
        List<EvalScore> scores = getStudentScores(studentId, courseId);

        // Get all weights for this course
        QueryWrapper<CourseEvalWeight> weightWrapper = new QueryWrapper<>();
        weightWrapper.eq("course_id", courseId);
        List<CourseEvalWeight> weights = courseEvalWeightMapper.selectList(weightWrapper);

        if (scores.isEmpty() || weights.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (CourseEvalWeight weight : weights) {
            // Find the score for this dimension
            EvalScore matchingScore = scores.stream()
                    .filter(s -> s.getDimensionId().equals(weight.getDimensionId()))
                    .findFirst()
                    .orElse(null);

            if (matchingScore != null && matchingScore.getScore() != null) {
                totalWeightedScore = totalWeightedScore.add(
                        matchingScore.getScore().multiply(weight.getWeight())
                );
                totalWeight = totalWeight.add(weight.getWeight());
            }
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }
}
