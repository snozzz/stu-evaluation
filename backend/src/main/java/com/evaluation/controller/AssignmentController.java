package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluation.entity.Assignment;
import com.evaluation.entity.AssignmentSubmission;
import com.evaluation.entity.SysUser;
import com.evaluation.service.AssignmentService;
import com.evaluation.service.AssignmentSubmissionService;
import com.evaluation.service.SysUserService;
import com.evaluation.util.IdResetUtil;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/api/assignment")
public class AssignmentController {

    @Resource
    private AssignmentService assignmentService;

    @Resource
    private AssignmentSubmissionService assignmentSubmissionService;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private IdResetUtil idResetUtil;

    @PostMapping("/create")
    public Result<?> create(@RequestBody Assignment assignment) {
        assignment.setCreateTime(new Date());
        boolean saved = assignmentService.save(assignment);
        return saved ? Result.success("布置成功", null) : Result.error("布置失败");
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam Long courseId,
                          @RequestParam(required = false) String type) {
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Assignment::getCourseId, courseId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Assignment::getType, type);
        }
        wrapper.orderByDesc(Assignment::getCreateTime);
        List<Assignment> list = assignmentService.list(wrapper);
        return Result.success(list);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed = assignmentService.removeById(id);
        if (removed) {
            idResetUtil.resetAutoIncrement("assignment");
            return Result.success();
        }
        return Result.error("删除失败");
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody AssignmentSubmission submission) {
        Assignment assignment = assignmentService.getById(submission.getAssignmentId());
        if (assignment == null) {
            return Result.error("作业/实验不存在");
        }
        Date now = new Date();
        if (assignment.getDueDate() != null && now.after(assignment.getDueDate())) {
            return Result.error("已超过截止时间，无法提交");
        }

        submission.setSubmitTime(new Date());
        if (submission.getStatus() == null) {
            submission.setStatus("SUBMITTED");
        }
        // Check if already submitted, update if so
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, submission.getAssignmentId());
        wrapper.eq(AssignmentSubmission::getStudentId, submission.getStudentId());
        AssignmentSubmission existing = assignmentSubmissionService.getOne(wrapper);
        if (existing != null) {
            submission.setId(existing.getId());
            assignmentSubmissionService.updateById(submission);
        } else {
            assignmentSubmissionService.save(submission);
        }
        return Result.success("提交成功", null);
    }

    @GetMapping("/submissions")
    public Result<?> submissions(@RequestParam Long assignmentId) {
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId);
        wrapper.orderByDesc(AssignmentSubmission::getSubmitTime);
        List<AssignmentSubmission> list = assignmentSubmissionService.list(wrapper);

        Set<Long> studentIds = new HashSet<>();
        for (AssignmentSubmission submission : list) {
            if (submission.getStudentId() != null) {
                studentIds.add(submission.getStudentId());
            }
        }

        Map<Long, String> studentNameMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<SysUser> students = sysUserService.listByIds(studentIds);
            for (SysUser student : students) {
                String name = student.getRealName() != null && !student.getRealName().trim().isEmpty()
                        ? student.getRealName()
                        : student.getNickname();
                studentNameMap.put(student.getId(), name != null && !name.trim().isEmpty() ? name : "未知学生");
            }
        }

        for (AssignmentSubmission submission : list) {
            submission.setStudentName(studentNameMap.getOrDefault(submission.getStudentId(), "未知学生"));
        }

        return Result.success(list);
    }

    @GetMapping("/student")
    public Result<?> studentAssignments(@RequestParam Long studentId,
                                        @RequestParam(required = false) Long courseId) {
        LambdaQueryWrapper<Assignment> assignWrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            assignWrapper.eq(Assignment::getCourseId, courseId);
        }
        assignWrapper.orderByDesc(Assignment::getCreateTime);
        List<Assignment> assignments = assignmentService.list(assignWrapper);

        // Get all submissions for this student
        LambdaQueryWrapper<AssignmentSubmission> subWrapper = new LambdaQueryWrapper<>();
        subWrapper.eq(AssignmentSubmission::getStudentId, studentId);
        List<AssignmentSubmission> submissions = assignmentSubmissionService.list(subWrapper);

        Map<Long, AssignmentSubmission> subMap = new HashMap<>();
        for (AssignmentSubmission sub : submissions) {
            subMap.put(sub.getAssignmentId(), sub);
        }

        Date now = new Date();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Assignment a : assignments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("courseId", a.getCourseId());
            item.put("teacherId", a.getTeacherId());
            item.put("title", a.getTitle());
            item.put("description", a.getDescription());
            item.put("type", a.getType());
            item.put("dueDate", a.getDueDate());
            item.put("createTime", a.getCreateTime());
            item.put("expired", a.getDueDate() != null && now.after(a.getDueDate()));
            AssignmentSubmission sub = subMap.get(a.getId());
            if (sub != null) {
                item.put("submitted", true);
                item.put("submissionId", sub.getId());
                item.put("submitTime", sub.getSubmitTime());
                item.put("status", sub.getStatus());
                item.put("score", sub.getScore());
                item.put("feedback", sub.getFeedback());
                item.put("fileUrl", sub.getFileUrl());
                item.put("content", sub.getContent());
            } else {
                item.put("submitted", false);
            }
            result.add(item);
        }

        return Result.success(result);
    }

    @PostMapping("/grade")
    public Result<?> grade(@RequestBody AssignmentSubmission submission) {
        AssignmentSubmission update = new AssignmentSubmission();
        update.setId(submission.getId());
        update.setScore(submission.getScore());
        update.setFeedback(submission.getFeedback());
        update.setStatus("GRADED");
        boolean updated = assignmentSubmissionService.updateById(update);
        return updated ? Result.success("评分成功", null) : Result.error("评分失败");
    }
}
