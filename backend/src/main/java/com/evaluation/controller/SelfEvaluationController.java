package com.evaluation.controller;

import com.evaluation.entity.SelfEvaluation;
import com.evaluation.service.SelfEvaluationService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/selfEval")
public class SelfEvaluationController {

    @Resource
    private SelfEvaluationService selfEvaluationService;

    @GetMapping("/list")
    public Result<?> list(@RequestParam Long studentId,
                          @RequestParam Long courseId) {
        List<SelfEvaluation> list = selfEvaluationService.getByStudentAndCourse(studentId, courseId);
        return Result.success(list);
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody SelfEvaluation selfEvaluation) {
        boolean saved = selfEvaluationService.save(selfEvaluation);
        return saved ? Result.success() : Result.error("保存失败");
    }

    @PostMapping("/batch")
    public Result<?> saveBatch(@RequestBody List<SelfEvaluation> selfEvaluations) {
        boolean saved = selfEvaluationService.saveBatch(selfEvaluations);
        return saved ? Result.success() : Result.error("批量保存失败");
    }
}
