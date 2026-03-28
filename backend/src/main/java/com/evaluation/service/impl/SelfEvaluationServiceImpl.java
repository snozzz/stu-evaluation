package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.SelfEvaluation;
import com.evaluation.mapper.SelfEvaluationMapper;
import com.evaluation.service.SelfEvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SelfEvaluationServiceImpl extends ServiceImpl<SelfEvaluationMapper, SelfEvaluation> implements SelfEvaluationService {

    @Resource
    private SelfEvaluationMapper selfEvaluationMapper;

    @Override
    public List<SelfEvaluation> getByStudentAndCourse(Long studentId, Long courseId) {
        QueryWrapper<SelfEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        queryWrapper.eq("course_id", courseId);
        queryWrapper.orderByDesc("update_time");
        queryWrapper.orderByDesc("id");

        List<SelfEvaluation> evaluations = selfEvaluationMapper.selectList(queryWrapper);
        Map<Long, SelfEvaluation> latestByDimension = new LinkedHashMap<>();
        for (SelfEvaluation evaluation : evaluations) {
            if (evaluation.getDimensionId() != null && !latestByDimension.containsKey(evaluation.getDimensionId())) {
                latestByDimension.put(evaluation.getDimensionId(), evaluation);
            }
        }
        return new ArrayList<>(latestByDimension.values());
    }

    @Override
    @Transactional
    public boolean saveOrUpdateByBizKey(SelfEvaluation selfEvaluation) {
        if (selfEvaluation == null) {
            return false;
        }

        QueryWrapper<SelfEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", selfEvaluation.getStudentId());
        queryWrapper.eq("course_id", selfEvaluation.getCourseId());
        queryWrapper.eq("dimension_id", selfEvaluation.getDimensionId());
        queryWrapper.orderByDesc("update_time");
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit 1");

        SelfEvaluation existing = selfEvaluationMapper.selectOne(queryWrapper);
        if (existing != null) {
            selfEvaluation.setId(existing.getId());
            return selfEvaluationMapper.updateById(selfEvaluation) > 0;
        }

        return selfEvaluationMapper.insert(selfEvaluation) > 0;
    }

    @Override
    @Transactional
    public boolean saveOrUpdateBatchByBizKey(List<SelfEvaluation> selfEvaluations) {
        if (selfEvaluations == null || selfEvaluations.isEmpty()) {
            return true;
        }

        for (SelfEvaluation selfEvaluation : selfEvaluations) {
            if (!saveOrUpdateByBizKey(selfEvaluation)) {
                return false;
            }
        }
        return true;
    }
}
