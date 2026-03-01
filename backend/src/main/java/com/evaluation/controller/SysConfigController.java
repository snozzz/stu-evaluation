package com.evaluation.controller;

import com.evaluation.entity.SysConfig;
import com.evaluation.service.SysConfigService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/config")
public class SysConfigController {

    @Resource
    private SysConfigService sysConfigService;

    @GetMapping("/list")
    public Result<?> list() {
        List<SysConfig> list = sysConfigService.list();
        return Result.success(list);
    }

    @GetMapping("/{key}")
    public Result<?> getByKey(@PathVariable String key) {
        SysConfig config = sysConfigService.getByKey(key);
        return config != null ? Result.success(config) : Result.error("配置不存在");
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody SysConfig config) {
        try {
            sysConfigService.updateByKey(config.getConfigKey(), config.getConfigValue());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
