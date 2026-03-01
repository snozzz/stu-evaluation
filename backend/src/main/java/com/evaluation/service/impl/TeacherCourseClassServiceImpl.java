package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.TeacherCourseClass;
import com.evaluation.mapper.TeacherCourseClassMapper;
import com.evaluation.service.TeacherCourseClassService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherCourseClassServiceImpl extends ServiceImpl<TeacherCourseClassMapper, TeacherCourseClass> implements TeacherCourseClassService {

    @Resource
    private TeacherCourseClassMapper teacherCourseClassMapper;

    @Override
    public List<TeacherCourseClass> getByTeacherId(Long teacherId) {
        QueryWrapper<TeacherCourseClass> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", teacherId);
        return teacherCourseClassMapper.selectList(queryWrapper);
    }

    @Override
    public List<Long> getCoursesByTeacher(Long teacherId) {
        QueryWrapper<TeacherCourseClass> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", teacherId);
        queryWrapper.select("DISTINCT course_id");
        List<TeacherCourseClass> list = teacherCourseClassMapper.selectList(queryWrapper);
        return list.stream()
                .map(TeacherCourseClass::getCourseId)
                .collect(Collectors.toList());
    }
}
