package com.evaluation.controller;

import com.evaluation.entity.TeacherCourseClass;
import com.evaluation.service.TeacherCourseClassService;
import com.evaluation.util.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/bindCourse")
public class TeacherCourseClassController {

    @Resource
    private TeacherCourseClassService teacherCourseClassService;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Long teacherId) {
        if (teacherId == null) {
            teacherId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        List<TeacherCourseClass> list = teacherCourseClassService.getByTeacherId(teacherId);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody TeacherCourseClass binding) {
        boolean saved = teacherCourseClassService.save(binding);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed = teacherCourseClassService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }
}
