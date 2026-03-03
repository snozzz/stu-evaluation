package com.evaluation.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardDTO {

    private Long totalStudents;

    private Long totalTeachers;

    private Long totalCourses;

    private Double evalCompletionRate;

    private List<Map<String, Object>> courseEvalStats;

    private List<Map<String, Object>> majorProgressStats;

    private Long evaluatedClassCount;

    private Long pendingClassCount;

    private Long alertCount;
}
