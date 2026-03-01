package com.evaluation.controller;

import com.evaluation.entity.CourseEvalWeight;
import com.evaluation.service.CourseEvalWeightService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/weight")
public class CourseEvalWeightController {

    @Resource
    private CourseEvalWeightService courseEvalWeightService;

    @GetMapping("/list/{courseId}")
    public Result<?> list(@PathVariable Long courseId) {
        List<CourseEvalWeight> list = courseEvalWeightService.getByCourseId(courseId);
        return Result.success(list);
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody List<CourseEvalWeight> weights) {
        boolean saved = courseEvalWeightService.saveOrUpdateBatch(weights);
        return saved ? Result.success() : Result.error("保存失败");
    }
}
