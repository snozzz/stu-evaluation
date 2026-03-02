package com.evaluation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.Assignment;
import com.evaluation.mapper.AssignmentMapper;
import com.evaluation.service.AssignmentService;
import org.springframework.stereotype.Service;

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {
}
