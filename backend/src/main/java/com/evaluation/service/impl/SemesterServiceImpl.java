package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.Semester;
import com.evaluation.mapper.SemesterMapper;
import com.evaluation.service.SemesterService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {

    @Resource
    private SemesterMapper semesterMapper;

    @Override
    public Semester getCurrentSemester() {
        QueryWrapper<Semester> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_current", 1);
        return semesterMapper.selectOne(queryWrapper);
    }
}
