package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.Appeal;
import com.evaluation.mapper.AppealMapper;
import com.evaluation.service.AppealService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {

    @Resource
    private AppealMapper appealMapper;

    @Override
    public List<Appeal> getByStudentId(Long studentId) {
        QueryWrapper<Appeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        return appealMapper.selectList(queryWrapper);
    }

    @Override
    public List<Appeal> getByTeacherId(Long teacherId) {
        QueryWrapper<Appeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", teacherId);
        return appealMapper.selectList(queryWrapper);
    }

    @Override
    public void reply(Long id, String reply) {
        Appeal appeal = new Appeal();
        appeal.setId(id);
        appeal.setReply(reply);
        appeal.setStatus("REPLIED");
        appeal.setUpdateTime(new Date());
        appealMapper.updateById(appeal);
    }
}
