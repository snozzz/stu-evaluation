package com.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.entity.SysConfig;

public interface SysConfigService extends IService<SysConfig> {

    SysConfig getByKey(String key);

    void updateByKey(String key, String value);
}
