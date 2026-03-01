package com.evaluation.controller;

import com.evaluation.entity.Appeal;
import com.evaluation.service.AppealService;
import com.evaluation.util.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appeal")
public class AppealController {

    @Resource
    private AppealService appealService;

    @GetMapping("/student")
    public Result<?> getStudentAppeals() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Appeal> list = appealService.getByStudentId(userId);
        return Result.success(list);
    }

    @GetMapping("/teacher")
    public Result<?> getTeacherAppeals() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Appeal> list = appealService.getByTeacherId(userId);
        return Result.success(list);
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Appeal appeal) {
        boolean saved = appealService.save(appeal);
        return saved ? Result.success() : Result.error("提交失败");
    }

    @PutMapping("/reply/{id}")
    public Result<?> reply(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reply = params.get("reply");
        try {
            appealService.reply(id, reply);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
