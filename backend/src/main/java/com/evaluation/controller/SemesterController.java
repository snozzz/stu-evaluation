package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluation.entity.Semester;
import com.evaluation.service.SemesterService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/semester")
public class SemesterController {

    @Resource
    private SemesterService semesterService;

    @GetMapping("/list")
    public Result<?> list() {
        List<Semester> list = semesterService.list();
        return Result.success(list);
    }

    @GetMapping("/current")
    public Result<?> getCurrent() {
        Semester current = semesterService.getCurrentSemester();
        return current != null ? Result.success(current) : Result.error("未设置当前学期");
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Semester semester) {
        boolean saved = semesterService.save(semester);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Semester semester) {
        semester.setId(id);
        boolean updated = semesterService.updateById(semester);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @PutMapping("/setCurrent/{id}")
    public Result<?> setCurrent(@PathVariable Long id) {
        // Set all semesters' isCurrent to 0
        Semester resetSemester = new Semester();
        resetSemester.setIsCurrent(0);
        semesterService.update(resetSemester, new LambdaQueryWrapper<>());

        // Set the target semester's isCurrent to 1
        Semester semester = new Semester();
        semester.setId(id);
        semester.setIsCurrent(1);
        boolean updated = semesterService.updateById(semester);
        return updated ? Result.success() : Result.error("设置失败");
    }
}
