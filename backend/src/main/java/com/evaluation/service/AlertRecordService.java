package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.AlertRecord;

import java.util.List;

public interface AlertRecordService extends IService<AlertRecord> {

    List<AlertRecord> getUnreadByUserId(Long userId);

    void markAsRead(Long id);
}
