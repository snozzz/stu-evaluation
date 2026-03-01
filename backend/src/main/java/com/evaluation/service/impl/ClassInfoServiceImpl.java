package com.evaluation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.ClassInfo;
import com.evaluation.mapper.ClassInfoMapper;
import com.evaluation.service.ClassInfoService;
import org.springframework.stereotype.Service;

@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {
}
