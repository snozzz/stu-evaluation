package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluation.entity.ClassInfo;
import com.evaluation.entity.StudentClass;
import com.evaluation.entity.SysUser;
import com.evaluation.service.ClassInfoService;
import com.evaluation.service.StudentClassService;
import com.evaluation.service.SysUserService;
import com.evaluation.util.IdResetUtil;
import com.evaluation.util.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/class")
public class ClassController {

    @Resource
    private ClassInfoService classInfoService;

    @Resource
    private StudentClassService studentClassService;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private IdResetUtil idResetUtil;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String keyword) {
        Page<ClassInfo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ClassInfo::getName, keyword);
        }
        wrapper.orderByAsc(ClassInfo::getId);
        Page<ClassInfo> result = classInfoService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody ClassInfo classInfo) {
        boolean saved = classInfoService.save(classInfo);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ClassInfo classInfo) {
        classInfo.setId(id);
        boolean updated = classInfoService.updateById(classInfo);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed = classInfoService.removeById(id);
        if (removed) {
            idResetUtil.resetAutoIncrement("class_info");
            return Result.success();
        }
        return Result.error("删除失败");
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        ClassInfo classInfo = classInfoService.getById(id);
        return classInfo != null ? Result.success(classInfo) : Result.error("班级不存在");
    }

    @GetMapping("/{classId}/students")
    public Result<?> getStudentsByClass(@PathVariable Long classId) {
        LambdaQueryWrapper<StudentClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentClass::getClassId, classId);
        List<StudentClass> scList = studentClassService.list(wrapper);
        List<Long> studentIds = scList.stream().map(StudentClass::getStudentId).collect(Collectors.toList());
        if (studentIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<SysUser> students = sysUserService.listByIds(studentIds);
        return Result.success(students);
    }
}
