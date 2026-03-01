package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluation.entity.Course;
import com.evaluation.service.CourseService;
import com.evaluation.util.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Resource
    private CourseService courseService;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String keyword) {
        Page<Course> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getName, keyword)
                    .or().like(Course::getCode, keyword);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        Page<Course> result = courseService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Course course) {
        boolean saved = courseService.save(course);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        boolean updated = courseService.updateById(course);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed = courseService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Course course = courseService.getById(id);
        return course != null ? Result.success(course) : Result.error("课程不存在");
    }
}
