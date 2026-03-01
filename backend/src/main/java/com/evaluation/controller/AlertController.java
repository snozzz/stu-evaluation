package com.evaluation.controller;

import com.evaluation.entity.AlertRecord;
import com.evaluation.entity.AlertRule;
import com.evaluation.service.AlertRecordService;
import com.evaluation.service.AlertRuleService;
import com.evaluation.util.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/alert")
public class AlertController {

    @Resource
    private AlertRuleService alertRuleService;

    @Resource
    private AlertRecordService alertRecordService;

    @GetMapping("/rules")
    public Result<?> listRules() {
        List<AlertRule> list = alertRuleService.list();
        return Result.success(list);
    }

    @PostMapping("/rule")
    public Result<?> addRule(@RequestBody AlertRule rule) {
        boolean saved = alertRuleService.save(rule);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @PutMapping("/rule/{id}")
    public Result<?> updateRule(@PathVariable Long id, @RequestBody AlertRule rule) {
        rule.setId(id);
        boolean updated = alertRuleService.updateById(rule);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/rule/{id}")
    public Result<?> deleteRule(@PathVariable Long id) {
        boolean removed = alertRuleService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }

    @GetMapping("/records")
    public Result<?> getRecords() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<AlertRecord> list = alertRecordService.getUnreadByUserId(userId);
        return Result.success(list);
    }

    @PutMapping("/record/read/{id}")
    public Result<?> markAsRead(@PathVariable Long id) {
        try {
            alertRecordService.markAsRead(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
