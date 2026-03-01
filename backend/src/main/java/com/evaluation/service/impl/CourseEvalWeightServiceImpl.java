package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.CourseEvalWeight;
import com.evaluation.mapper.CourseEvalWeightMapper;
import com.evaluation.service.CourseEvalWeightService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CourseEvalWeightServiceImpl extends ServiceImpl<CourseEvalWeightMapper, CourseEvalWeight> implements CourseEvalWeightService {

    @Resource
    private CourseEvalWeightMapper courseEvalWeightMapper;

    @Override
    public List<CourseEvalWeight> getByCourseId(Long courseId) {
        QueryWrapper<CourseEvalWeight> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return courseEvalWeightMapper.selectList(queryWrapper);
    }
}
