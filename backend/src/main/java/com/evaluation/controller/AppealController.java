package com.evaluation.controller;

import com.evaluation.entity.Appeal;
import com.evaluation.entity.Course;
import com.evaluation.entity.SysUser;
import com.evaluation.service.AppealService;
import com.evaluation.service.CourseService;
import com.evaluation.service.SysUserService;
import com.evaluation.util.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appeal")
public class AppealController {

    @Resource
    private AppealService appealService;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private CourseService courseService;

    @GetMapping("/student")
    public Result<?> getStudentAppeals() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Appeal> list = appealService.getByStudentId(userId);
        fillNames(list);
        return Result.success(list);
    }

    @GetMapping("/teacher")
    public Result<?> getTeacherAppeals() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Appeal> list = appealService.getByTeacherId(userId);
        fillNames(list);
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

    /**
     * Batch fill studentName and courseName for a list of appeals.
     */
    private void fillNames(List<Appeal> appeals) {
        if (appeals == null || appeals.isEmpty()) return;

        // Collect unique IDs
        Set<Long> studentIds = appeals.stream()
                .map(Appeal::getStudentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> courseIds = appeals.stream()
                .map(Appeal::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());

        // Batch query
        Map<Long, String> studentNameMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            sysUserService.listByIds(studentIds).forEach(u ->
                    studentNameMap.put(u.getId(), u.getRealName() != null ? u.getRealName() : u.getNickname()));
        }

        Map<Long, String> courseNameMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            courseService.listByIds(courseIds).forEach(c ->
                    courseNameMap.put(c.getId(), c.getName()));
        }

        // Fill names
        for (Appeal a : appeals) {
            a.setStudentName(studentNameMap.getOrDefault(a.getStudentId(), "未知学生"));
            a.setCourseName(courseNameMap.getOrDefault(a.getCourseId(), "未知课程"));
        }
    }
}
