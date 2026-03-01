package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.AlertRecord;
import com.evaluation.mapper.AlertRecordMapper;
import com.evaluation.service.AlertRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AlertRecordServiceImpl extends ServiceImpl<AlertRecordMapper, AlertRecord> implements AlertRecordService {

    @Resource
    private AlertRecordMapper alertRecordMapper;

    @Override
    public List<AlertRecord> getUnreadByUserId(Long userId) {
        QueryWrapper<AlertRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_read", 0);
        return alertRecordMapper.selectList(queryWrapper);
    }

    @Override
    public void markAsRead(Long id) {
        AlertRecord record = new AlertRecord();
        record.setId(id);
        record.setIsRead(1);
        alertRecordMapper.updateById(record);
    }
}
