package com.evaluation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.EvalDimension;
import com.evaluation.mapper.EvalDimensionMapper;
import com.evaluation.service.EvalDimensionService;
import org.springframework.stereotype.Service;

@Service
public class EvalDimensionServiceImpl extends ServiceImpl<EvalDimensionMapper, EvalDimension> implements EvalDimensionService {
}
