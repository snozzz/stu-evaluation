package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluation.entity.AlertRecord;
import com.evaluation.entity.AlertRule;
import com.evaluation.entity.Assignment;
import com.evaluation.entity.AssignmentSubmission;
import com.evaluation.entity.EvalScore;
import com.evaluation.entity.SelfEvaluation;
import com.evaluation.entity.Course;
import com.evaluation.entity.StudentClass;
import com.evaluation.entity.TeacherCourseClass;
import com.evaluation.service.AlertRecordService;
import com.evaluation.service.AlertRuleService;
import com.evaluation.service.AssignmentService;
import com.evaluation.service.AssignmentSubmissionService;
import com.evaluation.service.EvalScoreService;
import com.evaluation.service.SelfEvaluationService;
import com.evaluation.service.CourseService;
import com.evaluation.service.StudentClassService;
import com.evaluation.service.TeacherCourseClassService;
import com.evaluation.util.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private StudentClassService studentClassService;

    @Resource
    private TeacherCourseClassService teacherCourseClassService;

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
        Date now = new Date();
        String nowText = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

        try {
            List<AlertRule> enabledRules = alertRuleService.list(
                    new LambdaQueryWrapper<AlertRule>().eq(AlertRule::getIsEnabled, 1)
            );
            Map<String, AlertRule> ruleMap = new HashMap<>();
            for (AlertRule rule : enabledRules) {
                ruleMap.put(rule.getRuleType(), rule);
            }

            List<StudentClass> studentClassLinks = studentClassService.getByStudentId(studentId);
            Set<Long> studentClassIds = studentClassLinks.stream()
                    .map(StudentClass::getClassId)
                    .collect(Collectors.toSet());

            Set<Long> enrolledCourseIds = new HashSet<>();
            if (!studentClassIds.isEmpty()) {
                LambdaQueryWrapper<TeacherCourseClass> bindingWrapper = new LambdaQueryWrapper<>();
                bindingWrapper.in(TeacherCourseClass::getClassId, studentClassIds);
                List<TeacherCourseClass> bindings = teacherCourseClassService.list(bindingWrapper);
                for (TeacherCourseClass binding : bindings) {
                    enrolledCourseIds.add(binding.getCourseId());
                }
            }

            LambdaQueryWrapper<EvalScore> scoreWrapper = new LambdaQueryWrapper<>();
            scoreWrapper.eq(EvalScore::getStudentId, studentId);
            List<EvalScore> studentScores = evalScoreService.list(scoreWrapper);
            for (EvalScore score : studentScores) {
                enrolledCourseIds.add(score.getCourseId());
            }

            // 1. 作业/实验：仅在截止后且未提交时提醒
            if (ruleMap.containsKey("HOMEWORK_MISSING") && !enrolledCourseIds.isEmpty()) {
                LambdaQueryWrapper<AssignmentSubmission> subWrapper = new LambdaQueryWrapper<>();
                subWrapper.eq(AssignmentSubmission::getStudentId, studentId);
                List<AssignmentSubmission> submissions = assignmentSubmissionService.list(subWrapper);
                Set<Long> submittedIds = submissions.stream()
                        .map(AssignmentSubmission::getAssignmentId)
                        .collect(Collectors.toSet());

                LambdaQueryWrapper<Assignment> assignmentWrapper = new LambdaQueryWrapper<>();
                assignmentWrapper.in(Assignment::getCourseId, enrolledCourseIds)
                        .isNotNull(Assignment::getDueDate);
                List<Assignment> assignments = assignmentService.list(assignmentWrapper);
                long seq = 1L;
                for (Assignment assignment : assignments) {
                    if (assignment.getDueDate() != null
                            && now.after(assignment.getDueDate())
                            && !submittedIds.contains(assignment.getId())) {
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("id", seq++);
                        alert.put("type", "MISSING_ASSIGNMENT");
                        alert.put("courseId", assignment.getCourseId());
                        alert.put("message", "作业/实验\"" + assignment.getTitle() + "\"已截止仍未提交，请尽快联系任课老师");
                        alert.put("createTime", nowText);
                        alerts.add(alert);
                    }
                }
            }

            // 2. 自评未完成提醒
            if (ruleMap.containsKey("EVAL_DELAY") && !enrolledCourseIds.isEmpty()) {
                LambdaQueryWrapper<SelfEvaluation> selfEvalWrapper = new LambdaQueryWrapper<>();
                selfEvalWrapper.eq(SelfEvaluation::getStudentId, studentId)
                        .in(SelfEvaluation::getCourseId, enrolledCourseIds);
                List<SelfEvaluation> selfEvals = selfEvaluationService.list(selfEvalWrapper);
                Map<Long, Set<Long>> selfEvalDimMap = new HashMap<>();
                for (SelfEvaluation selfEval : selfEvals) {
                    selfEvalDimMap
                            .computeIfAbsent(selfEval.getCourseId(), k -> new HashSet<Long>())
                            .add(selfEval.getDimensionId());
                }
                int requiredDimensionCount = (int) studentScores.stream()
                        .map(EvalScore::getDimensionId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
                if (requiredDimensionCount <= 0) {
                    requiredDimensionCount = 1;
                }

                long seq = alerts.size() + 1L;
                for (Long courseId : enrolledCourseIds) {
                    int doneCount = selfEvalDimMap.containsKey(courseId) ? selfEvalDimMap.get(courseId).size() : 0;
                    if (doneCount < requiredDimensionCount) {
                        Course course = courseService.getById(courseId);
                        String courseName = course != null ? course.getName() : "未知课程";
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("id", seq++);
                        alert.put("type", "MISSING_SELF_EVAL");
                        alert.put("courseId", courseId);
                        alert.put("message", "课程\"" + courseName + "\"自评未完成（" + doneCount + "/" + requiredDimensionCount + "）");
                        alert.put("createTime", nowText);
                        alerts.add(alert);
                    }
                }
            }

            // 3. 成绩明显低于班级平均提醒
            if (ruleMap.containsKey("SCORE_LOW") && !studentScores.isEmpty()) {
                AlertRule scoreRule = ruleMap.get("SCORE_LOW");
                double diffThreshold = 10.0;
                Double absoluteThreshold = null;
                if (scoreRule != null && scoreRule.getThreshold() != null) {
                    double threshold = scoreRule.getThreshold().doubleValue();
                    if (threshold > 0 && threshold <= 30) {
                        diffThreshold = threshold;
                    } else if (threshold > 30) {
                        absoluteThreshold = threshold;
                    }
                }

                Map<Long, List<EvalScore>> studentCourseScoreMap = studentScores.stream()
                        .collect(Collectors.groupingBy(EvalScore::getCourseId));
                long seq = alerts.size() + 1L;
                for (Map.Entry<Long, List<EvalScore>> entry : studentCourseScoreMap.entrySet()) {
                    Long courseId = entry.getKey();
                    double studentAvg = entry.getValue().stream()
                            .map(EvalScore::getScore)
                            .filter(Objects::nonNull)
                            .mapToDouble(BigDecimal::doubleValue)
                            .average()
                            .orElse(0.0);
                    if (studentAvg <= 0) {
                        continue;
                    }

                    List<Long> classmateIds = getClassmatesByCourse(studentId, courseId, studentClassIds);
                    double classAvg = calculateClassCourseAverage(classmateIds, courseId);
                    if (classAvg <= 0) {
                        continue;
                    }

                    boolean lowerThanClass = classAvg - studentAvg >= diffThreshold;
                    boolean lowerThanAbsolute = absoluteThreshold != null && studentAvg < absoluteThreshold && studentAvg < classAvg;
                    if (lowerThanClass || lowerThanAbsolute) {
                        Course course = courseService.getById(courseId);
                        String courseName = course != null ? course.getName() : "未知课程";
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("id", seq++);
                        alert.put("type", "LOW_SCORE");
                        alert.put("courseId", courseId);
                        alert.put("message", "课程\"" + courseName + "\"成绩偏低（你: "
                                + String.format("%.1f", studentAvg) + "，班均: "
                                + String.format("%.1f", classAvg) + "）");
                        alert.put("createTime", nowText);
                        alerts.add(alert);
                    }
                }
            }
        } catch (Exception e) {
            // Return whatever alerts we have so far
        }

        return Result.success(alerts);
    }

    private List<Long> getClassmatesByCourse(Long studentId, Long courseId, Set<Long> studentClassIds) {
        Set<Long> targetClassIds = new HashSet<>();
        if (!studentClassIds.isEmpty()) {
            LambdaQueryWrapper<TeacherCourseClass> bindingWrapper = new LambdaQueryWrapper<>();
            bindingWrapper.eq(TeacherCourseClass::getCourseId, courseId)
                    .in(TeacherCourseClass::getClassId, studentClassIds);
            List<TeacherCourseClass> bindings = teacherCourseClassService.list(bindingWrapper);
            for (TeacherCourseClass binding : bindings) {
                targetClassIds.add(binding.getClassId());
            }
        }

        if (targetClassIds.isEmpty() && !studentClassIds.isEmpty()) {
            targetClassIds.addAll(studentClassIds);
        }

        if (!targetClassIds.isEmpty()) {
            LambdaQueryWrapper<StudentClass> classmateWrapper = new LambdaQueryWrapper<>();
            classmateWrapper.in(StudentClass::getClassId, targetClassIds);
            List<StudentClass> classmates = studentClassService.list(classmateWrapper);
            List<Long> ids = classmates.stream()
                    .map(StudentClass::getStudentId)
                    .distinct()
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                return ids;
            }
        }

        // Fallback: 至少包含自己，避免无班级数据时无法计算
        return Collections.singletonList(studentId);
    }

    private double calculateClassCourseAverage(List<Long> studentIds, Long courseId) {
        if (studentIds == null || studentIds.isEmpty()) {
            return 0.0;
        }
        LambdaQueryWrapper<EvalScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvalScore::getCourseId, courseId)
                .in(EvalScore::getStudentId, studentIds);
        List<EvalScore> scores = evalScoreService.list(wrapper);
        if (scores.isEmpty()) {
            return 0.0;
        }

        Map<Long, List<EvalScore>> byStudent = scores.stream()
                .collect(Collectors.groupingBy(EvalScore::getStudentId));
        return byStudent.values().stream()
                .mapToDouble(items -> items.stream()
                        .map(EvalScore::getScore)
                        .filter(Objects::nonNull)
                        .mapToDouble(BigDecimal::doubleValue)
                        .average()
                        .orElse(0.0))
                .filter(v -> v > 0)
                .average()
                .orElse(0.0);
    }
}
