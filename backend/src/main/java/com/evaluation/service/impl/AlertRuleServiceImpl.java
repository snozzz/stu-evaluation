package com.evaluation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.AlertRule;
import com.evaluation.mapper.AlertRuleMapper;
import com.evaluation.service.AlertRuleService;
import org.springframework.stereotype.Service;

@Service
public class AlertRuleServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRule> implements AlertRuleService {
}
