package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluation.entity.AlertRecord;
import com.evaluation.entity.AlertRule;
import com.evaluation.entity.Assignment;
import com.evaluation.entity.AssignmentSubmission;
import com.evaluation.entity.EvalScore;
import com.evaluation.entity.SelfEvaluation;
import com.evaluation.entity.Course;
import com.evaluation.service.AlertRecordService;
import com.evaluation.service.AlertRuleService;
import com.evaluation.service.AssignmentService;
import com.evaluation.service.AssignmentSubmissionService;
import com.evaluation.service.EvalScoreService;
import com.evaluation.service.SelfEvaluationService;
import com.evaluation.service.CourseService;
import com.evaluation.util.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/api/alert")
public class AlertController {

    @Resource
    private AlertRuleService alertRuleService;

    @Resource
    private AlertRecordService alertRecordService;

    @Resource
    private AssignmentService assignmentService;

    @Resource
    private AssignmentSubmissionService assignmentSubmissionService;

    @Resource
    private EvalScoreService evalScoreService;

    @Resource
    private SelfEvaluationService selfEvaluationService;

    @Resource
    private CourseService courseService;

    @GetMapping("/rules")
    public Result<?> listRules() {
        List<AlertRule> list = alertRuleService.list();
        return Result.success(list);
    }

    @PostMapping("/rule")
    public Result<?> addRule(@RequestBody AlertRule rule) {
        boolean saved = alertRuleService.save(rule);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @PutMapping("/rule/{id}")
    public Result<?> updateRule(@PathVariable Long id, @RequestBody AlertRule rule) {
        rule.setId(id);
        boolean updated = alertRuleService.updateById(rule);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/rule/{id}")
    public Result<?> deleteRule(@PathVariable Long id) {
        boolean removed = alertRuleService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }

    @GetMapping("/records")
    public Result<?> getRecords() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<AlertRecord> list = alertRecordService.getUnreadByUserId(userId);
        return Result.success(list);
    }

    @PutMapping("/record/read/{id}")
    public Result<?> markAsRead(@PathVariable Long id) {
        try {
            alertRecordService.markAsRead(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public Result<?> getStudentAlerts(@PathVariable Long studentId) {
        List<Map<String, Object>> alerts = new ArrayList<>();

        try {
            // 1. Check for overdue unsubmitted assignments
            List<Assignment> allAssignments = assignmentService.list();
            LambdaQueryWrapper<AssignmentSubmission> subWrapper = new LambdaQueryWrapper<>();
            subWrapper.eq(AssignmentSubmission::getStudentId, studentId);
            List<AssignmentSubmission> submissions = assignmentSubmissionService.list(subWrapper);
            Set<Long> submittedIds = new HashSet<>();
            for (AssignmentSubmission sub : submissions) {
                submittedIds.add(sub.getAssignmentId());
            }

            Date now = new Date();
            for (Assignment a : allAssignments) {
                if (!submittedIds.contains(a.getId())) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "MISSING_ASSIGNMENT");
                    alert.put("message", "作业/实验\"" + a.getTitle() + "\"尚未提交，请及时处理");
                    alert.put("courseId", a.getCourseId());
                    alerts.add(alert);
                }
            }

            // 2. Check for missing self evaluations
            // Assuming student should have self-evaluated all courses they are in
            // But we need to know what courses the student has. We can get it from their scores or self-evals.
            // Simplified: let's get the courses from the student's existing scores
            LambdaQueryWrapper<EvalScore> scoreWrapper = new LambdaQueryWrapper<>();
            scoreWrapper.eq(EvalScore::getStudentId, studentId);
            List<EvalScore> studentScores = evalScoreService.list(scoreWrapper);

            Set<Long> courseIds = new HashSet<>();
            for (EvalScore s : studentScores) {
                courseIds.add(s.getCourseId());
            }

            LambdaQueryWrapper<SelfEvaluation> selfEvalWrapper = new LambdaQueryWrapper<>();
            selfEvalWrapper.eq(SelfEvaluation::getStudentId, studentId);
            List<SelfEvaluation> selfEvals = selfEvaluationService.list(selfEvalWrapper);
            Set<Long> selfEvalCourseIds = new HashSet<>();
            for (SelfEvaluation se : selfEvals) {
                selfEvalCourseIds.add(se.getCourseId());
            }

            for (Long cid : courseIds) {
                if (!selfEvalCourseIds.contains(cid)) {
                    Course course = courseService.getById(cid);
                    String courseName = course != null ? course.getName() : "未知课程";
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "MISSING_SELF_EVAL");
                    alert.put("message", "课程\"" + courseName + "\"尚未完成自我评价");
                    alert.put("courseId", cid);
                    alerts.add(alert);
                }
            }

            // 3. Check for low scores compared to class average

            // Group by courseId and compute average
            Map<Long, List<EvalScore>> courseScoreMap = new HashMap<>();
            for (EvalScore s : studentScores) {
                courseScoreMap.computeIfAbsent(s.getCourseId(), k -> new ArrayList<>()).add(s);
            }

            for (Map.Entry<Long, List<EvalScore>> entry : courseScoreMap.entrySet()) {
                double avg = entry.getValue().stream()
                    .mapToDouble(s -> s.getScore() != null ? s.getScore().doubleValue() : 0)
                    .average().orElse(0);
                if (avg < 60) {
                    Course course = courseService.getById(entry.getKey());
                    String courseName = course != null ? course.getName() : "未知课程";
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "LOW_SCORE");
                    alert.put("message", "课程\"" + courseName + "\"成绩偏低（均分" + String.format("%.1f", avg) + "），建议加强学习");
                    alert.put("courseId", entry.getKey());
                    alert.put("courseName", courseName);
                    alerts.add(alert);
                }
            }
        } catch (Exception e) {
            // Return whatever alerts we have so far
        }

        return Result.success(alerts);
    }
}
