package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluation.dto.DashboardDTO;
import com.evaluation.entity.AlertRecord;
import com.evaluation.entity.ClassInfo;
import com.evaluation.entity.Course;
import com.evaluation.entity.EvalScore;
import com.evaluation.entity.StudentClass;
import com.evaluation.entity.SysUser;
import com.evaluation.entity.TeacherCourseClass;
import com.evaluation.mapper.AlertRecordMapper;
import com.evaluation.mapper.ClassInfoMapper;
import com.evaluation.mapper.CourseMapper;
import com.evaluation.mapper.EvalScoreMapper;
import com.evaluation.mapper.StudentClassMapper;
import com.evaluation.mapper.SysUserMapper;
import com.evaluation.mapper.TeacherCourseClassMapper;
import com.evaluation.service.DashboardService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private EvalScoreMapper evalScoreMapper;

    @Resource
    private TeacherCourseClassMapper teacherCourseClassMapper;

    @Resource
    private StudentClassMapper studentClassMapper;

    @Resource
    private ClassInfoMapper classInfoMapper;

    @Resource
    private AlertRecordMapper alertRecordMapper;

    @Override
    public DashboardDTO getStats() {
        DashboardDTO dto = new DashboardDTO();

        QueryWrapper<SysUser> studentWrapper = new QueryWrapper<>();
        studentWrapper.eq("role", "STUDENT");
        dto.setTotalStudents(sysUserMapper.selectCount(studentWrapper));

        QueryWrapper<SysUser> teacherWrapper = new QueryWrapper<>();
        teacherWrapper.eq("role", "TEACHER");
        dto.setTotalTeachers(sysUserMapper.selectCount(teacherWrapper));

        List<Course> courses = courseMapper.selectList(null);
        dto.setTotalCourses((long) courses.size());

        List<TeacherCourseClass> bindings = teacherCourseClassMapper.selectList(null);
        List<StudentClass> studentClasses = studentClassMapper.selectList(null);
        List<EvalScore> allScores = evalScoreMapper.selectList(null);
        List<ClassInfo> classInfos = classInfoMapper.selectList(null);

        Map<Long, Set<Long>> classStudentMap = new HashMap<>();
        for (StudentClass sc : studentClasses) {
            classStudentMap.computeIfAbsent(sc.getClassId(), k -> new HashSet<Long>()).add(sc.getStudentId());
        }

        // classId -> evaluated courses
        Map<Long, Set<Long>> classEvaluatedCourseMap = new HashMap<>();
        // courseId -> evaluated classes
        Map<Long, Set<Long>> courseEvaluatedClassMap = new HashMap<>();
        // studentId+courseId existence index
        Set<String> studentCourseScoreSet = allScores.stream()
                .map(s -> s.getStudentId() + "_" + s.getCourseId())
                .collect(Collectors.toSet());

        for (TeacherCourseClass binding : bindings) {
            Long classId = binding.getClassId();
            Long courseId = binding.getCourseId();
            Set<Long> studentIds = classStudentMap.get(classId);
            if (studentIds == null || studentIds.isEmpty()) {
                continue;
            }
            boolean evaluated = false;
            for (Long studentId : studentIds) {
                if (studentCourseScoreSet.contains(studentId + "_" + courseId)) {
                    evaluated = true;
                    break;
                }
            }
            if (evaluated) {
                classEvaluatedCourseMap.computeIfAbsent(classId, k -> new HashSet<Long>()).add(courseId);
                courseEvaluatedClassMap.computeIfAbsent(courseId, k -> new HashSet<Long>()).add(classId);
            }
        }

        Set<Long> classIdsWithData = classEvaluatedCourseMap.keySet();
        long totalClasses = classInfos.size();
        long evaluatedClassCount = classIdsWithData.size();
        dto.setEvaluatedClassCount(evaluatedClassCount);
        dto.setPendingClassCount(Math.max(0, totalClasses - evaluatedClassCount));

        QueryWrapper<AlertRecord> alertWrapper = new QueryWrapper<>();
        alertWrapper.eq("is_read", 0);
        dto.setAlertCount(alertRecordMapper.selectCount(alertWrapper));

        long totalBindingPairs = bindings.stream()
                .map(b -> b.getCourseId() + "_" + b.getClassId())
                .distinct()
                .count();
        long evaluatedBindingPairs = classEvaluatedCourseMap.entrySet().stream()
                .mapToLong(e -> e.getValue().size())
                .sum();
        dto.setEvalCompletionRate(totalBindingPairs == 0
                ? 0.0
                : (double) evaluatedBindingPairs * 100.0 / (double) totalBindingPairs);

        // 1) 各课程评价完成率
        Map<Long, Set<Long>> courseAllClassMap = new HashMap<>();
        for (TeacherCourseClass binding : bindings) {
            courseAllClassMap.computeIfAbsent(binding.getCourseId(), k -> new HashSet<Long>()).add(binding.getClassId());
        }
        List<Map<String, Object>> courseEvalStats = new ArrayList<>();
        for (Course course : courses) {
            Set<Long> allClassSet = courseAllClassMap.getOrDefault(course.getId(), new HashSet<Long>());
            Set<Long> doneClassSet = courseEvaluatedClassMap.getOrDefault(course.getId(), new HashSet<Long>());
            int total = allClassSet.size();
            int done = doneClassSet.size();
            double completionRate = total == 0 ? 0.0 : (double) done * 100.0 / (double) total;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("courseId", course.getId());
            item.put("courseName", course.getName());
            item.put("totalClasses", total);
            item.put("evaluatedClasses", done);
            item.put("completionRate", completionRate);
            courseEvalStats.add(item);
        }
        dto.setCourseEvalStats(courseEvalStats);

        // 2) 各学院/专业进度占比（按学院/专业聚合）
        Map<Long, ClassInfo> classMap = classInfos.stream()
                .collect(Collectors.toMap(ClassInfo::getId, c -> c, (a, b) -> a));
        Map<String, List<TeacherCourseClass>> majorBindingMap = new HashMap<>();
        for (TeacherCourseClass binding : bindings) {
            ClassInfo cls = classMap.get(binding.getClassId());
            String college = cls != null && cls.getCollege() != null ? cls.getCollege() : "未知学院";
            String major = cls != null && cls.getMajor() != null ? cls.getMajor() : "未知专业";
            String key = college + "/" + major;
            majorBindingMap.computeIfAbsent(key, k -> new ArrayList<>()).add(binding);
        }

        int completed = 0, inProgress = 0, notStarted = 0;
        for (Map.Entry<String, List<TeacherCourseClass>> entry : majorBindingMap.entrySet()) {
            List<TeacherCourseClass> list = entry.getValue();
            int total = list.size();
            int done = 0;
            for (TeacherCourseClass binding : list) {
                Set<Long> doneCourses = classEvaluatedCourseMap.getOrDefault(binding.getClassId(), new HashSet<Long>());
                if (doneCourses.contains(binding.getCourseId())) {
                    done++;
                }
            }
            if (total == 0 || done == 0) {
                notStarted++;
            } else if (done == total) {
                completed++;
            } else {
                inProgress++;
            }
        }
        List<Map<String, Object>> majorProgressStats = new ArrayList<>();
        Map<String, Object> doneItem = new LinkedHashMap<>();
        doneItem.put("status", "已完成");
        doneItem.put("count", completed);
        majorProgressStats.add(doneItem);
        Map<String, Object> progressItem = new LinkedHashMap<>();
        progressItem.put("status", "进行中");
        progressItem.put("count", inProgress);
        majorProgressStats.add(progressItem);
        Map<String, Object> notItem = new LinkedHashMap<>();
        notItem.put("status", "未开始");
        notItem.put("count", notStarted);
        majorProgressStats.add(notItem);
        dto.setMajorProgressStats(majorProgressStats);

        return dto;
    }
}
