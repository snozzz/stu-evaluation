package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.StudentClass;
import com.evaluation.mapper.StudentClassMapper;
import com.evaluation.service.StudentClassService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StudentClassServiceImpl extends ServiceImpl<StudentClassMapper, StudentClass> implements StudentClassService {

    @Resource
    private StudentClassMapper studentClassMapper;

    @Override
    public List<StudentClass> getByStudentId(Long studentId) {
        QueryWrapper<StudentClass> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        return studentClassMapper.selectList(queryWrapper);
    }
}
