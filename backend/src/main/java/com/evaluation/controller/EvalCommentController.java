package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluation.entity.EvalComment;
import com.evaluation.entity.SysUser;
import com.evaluation.service.EvalCommentService;
import com.evaluation.service.SysUserService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comment")
public class EvalCommentController {

    @Resource
    private EvalCommentService evalCommentService;

    @Resource
    private SysUserService sysUserService;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Long studentId,
                          @RequestParam(required = false) Long courseId,
                          @RequestParam(required = false) Long teacherId) {
        LambdaQueryWrapper<EvalComment> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(EvalComment::getStudentId, studentId);
        }
        if (courseId != null) {
            wrapper.eq(EvalComment::getCourseId, courseId);
        }
        if (teacherId != null) {
            wrapper.eq(EvalComment::getTeacherId, teacherId);
        }
        wrapper.orderByDesc(EvalComment::getCreateTime);
        List<EvalComment> list = evalCommentService.list(wrapper);
        fillStudentNames(list);
        return Result.success(list);
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody EvalComment comment) {
        boolean ok = evalCommentService.saveOrUpdate(comment);
        return ok ? Result.success() : Result.error("保存失败");
    }

    @PostMapping("/ai-generate")
    public Result<?> generateAIComment(@RequestParam Long studentId,
                                       @RequestParam Long courseId) {
        try {
            String aiComment = evalCommentService.generateAIComment(studentId, courseId);
            return Result.success(aiComment);
        } catch (Exception e) {
            return Result.error("AI评语生成失败：" + e.getMessage());
        }
    }

    @PutMapping("/publish/{id}")
    public Result<?> publish(@PathVariable Long id) {
        EvalComment comment = new EvalComment();
        comment.setId(id);
        comment.setIsPublished(1);
        boolean updated = evalCommentService.updateById(comment);
        return updated ? Result.success() : Result.error("发布失败");
    }

    private void fillStudentNames(List<EvalComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        Set<Long> studentIds = comments.stream()
                .map(EvalComment::getStudentId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (studentIds.isEmpty()) {
            return;
        }

        HashMap<Long, String> studentNameMap = new HashMap<>();
        List<SysUser> students = sysUserService.listByIds(studentIds);
        for (SysUser student : students) {
            String name = student.getRealName();
            if (name == null || name.trim().isEmpty()) {
                name = student.getNickname();
            }
            studentNameMap.put(student.getId(), name);
        }

        for (EvalComment comment : comments) {
            String studentName = studentNameMap.get(comment.getStudentId());
            comment.setStudentName(
                    (studentName == null || studentName.trim().isEmpty()) ? "未知学生" : studentName
            );
        }
    }
}
