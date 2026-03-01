package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.EvalComment;

import java.util.List;

public interface EvalCommentService extends IService<EvalComment> {

    List<EvalComment> getByStudentAndCourse(Long studentId, Long courseId);

    String generateAIComment(Long studentId, Long courseId);
}
