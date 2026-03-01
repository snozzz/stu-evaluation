package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.Appeal;

import java.util.List;

public interface AppealService extends IService<Appeal> {

    List<Appeal> getByStudentId(Long studentId);

    List<Appeal> getByTeacherId(Long teacherId);

    void reply(Long id, String reply);
}
