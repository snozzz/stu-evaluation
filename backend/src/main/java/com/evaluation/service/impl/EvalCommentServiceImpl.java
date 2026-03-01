package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.EvalComment;
import com.evaluation.entity.EvalDimension;
import com.evaluation.entity.EvalScore;
import com.evaluation.entity.SysUser;
import com.evaluation.mapper.EvalCommentMapper;
import com.evaluation.mapper.EvalDimensionMapper;
import com.evaluation.mapper.EvalScoreMapper;
import com.evaluation.mapper.SysUserMapper;
import com.evaluation.service.EvalCommentService;
import com.evaluation.util.DeepSeekUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvalCommentServiceImpl extends ServiceImpl<EvalCommentMapper, EvalComment> implements EvalCommentService {

    @Resource
    private EvalCommentMapper evalCommentMapper;

    @Resource
    private DeepSeekUtil deepSeekUtil;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private EvalScoreMapper evalScoreMapper;

    @Resource
    private EvalDimensionMapper evalDimensionMapper;

    @Override
    public List<EvalComment> getByStudentAndCourse(Long studentId, Long courseId) {
        QueryWrapper<EvalComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        queryWrapper.eq("course_id", courseId);
        return evalCommentMapper.selectList(queryWrapper);
    }

    @Override
    public String generateAIComment(Long studentId, Long courseId) {
        // Get student name
        SysUser student = sysUserMapper.selectById(studentId);
        String studentName = student != null ? student.getRealName() : "未知学生";

        // Get scores for this student and course
        QueryWrapper<EvalScore> scoreWrapper = new QueryWrapper<>();
        scoreWrapper.eq("student_id", studentId);
        scoreWrapper.eq("course_id", courseId);
        List<EvalScore> scores = evalScoreMapper.selectList(scoreWrapper);

        // Get all dimensions for mapping dimension names
        List<EvalDimension> dimensions = evalDimensionMapper.selectList(null);
        Map<Long, String> dimensionNameMap = new HashMap<>();
        for (EvalDimension dim : dimensions) {
            dimensionNameMap.put(dim.getId(), dim.getName());
        }

        // Build scores map with dimension names
        Map<String, Object> scoreMap = new LinkedHashMap<>();
        for (EvalScore score : scores) {
            String dimensionName = dimensionNameMap.getOrDefault(score.getDimensionId(), "维度" + score.getDimensionId());
            scoreMap.put(dimensionName, score.getScore());
        }

        // Call DeepSeek API to generate comment
        return deepSeekUtil.generateComment(studentName, scoreMap);
    }
}
