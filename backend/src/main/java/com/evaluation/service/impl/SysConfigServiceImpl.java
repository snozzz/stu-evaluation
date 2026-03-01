package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.entity.SysConfig;
import com.evaluation.mapper.SysConfigMapper;
import com.evaluation.service.SysConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Resource
    private SysConfigMapper sysConfigMapper;

    @Override
    public SysConfig getByKey(String key) {
        QueryWrapper<SysConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", key);
        return sysConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateByKey(String key, String value) {
        QueryWrapper<SysConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", key);
        SysConfig config = sysConfigMapper.selectOne(queryWrapper);
        if (config != null) {
            config.setConfigValue(value);
            config.setUpdateTime(new Date());
            sysConfigMapper.updateById(config);
        }
    }
}
