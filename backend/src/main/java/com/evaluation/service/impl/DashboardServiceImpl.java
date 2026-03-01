package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluation.dto.DashboardDTO;
import com.evaluation.entity.SysUser;
import com.evaluation.mapper.CourseMapper;
import com.evaluation.mapper.EvalScoreMapper;
import com.evaluation.mapper.SysUserMapper;
import com.evaluation.service.DashboardService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private EvalScoreMapper evalScoreMapper;

    @Override
    public DashboardDTO getStats() {
        DashboardDTO dto = new DashboardDTO();

        QueryWrapper<SysUser> studentWrapper = new QueryWrapper<>();
        studentWrapper.eq("role", "STUDENT");
        dto.setTotalStudents(sysUserMapper.selectCount(studentWrapper));

        QueryWrapper<SysUser> teacherWrapper = new QueryWrapper<>();
        teacherWrapper.eq("role", "TEACHER");
        dto.setTotalTeachers(sysUserMapper.selectCount(teacherWrapper));

        dto.setTotalCourses(courseMapper.selectCount(null));

        Long totalScoreRecords = evalScoreMapper.selectCount(null);
        if (totalScoreRecords != null && totalScoreRecords > 0) {
            dto.setEvalCompletionRate(100.0);
        } else {
            dto.setEvalCompletionRate(0.0);
        }

        return dto;
    }
}
