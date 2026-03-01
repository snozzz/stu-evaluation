package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.SelfEvaluation;
import com.evaluation.mapper.SelfEvaluationMapper;
import com.evaluation.service.SelfEvaluationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SelfEvaluationServiceImpl extends ServiceImpl<SelfEvaluationMapper, SelfEvaluation> implements SelfEvaluationService {

    @Resource
    private SelfEvaluationMapper selfEvaluationMapper;

    @Override
    public List<SelfEvaluation> getByStudentAndCourse(Long studentId, Long courseId) {
        QueryWrapper<SelfEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        queryWrapper.eq("course_id", courseId);
        return selfEvaluationMapper.selectList(queryWrapper);
    }
}
