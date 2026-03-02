package com.evaluation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.AssignmentSubmission;
import com.evaluation.mapper.AssignmentSubmissionMapper;
import com.evaluation.service.AssignmentSubmissionService;
import org.springframework.stereotype.Service;

@Service
public class AssignmentSubmissionServiceImpl extends ServiceImpl<AssignmentSubmissionMapper, AssignmentSubmission> implements AssignmentSubmissionService {
}
