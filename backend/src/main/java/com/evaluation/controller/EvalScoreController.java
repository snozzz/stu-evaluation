package com.evaluation.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluation.dto.ScoreImportDTO;
import com.evaluation.entity.EvalDimension;
import com.evaluation.entity.EvalScore;
import com.evaluation.entity.SysUser;
import com.evaluation.service.EvalDimensionService;
import com.evaluation.service.EvalScoreService;
import com.evaluation.service.SysUserService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/score")
public class EvalScoreController {

    @Resource
    private EvalScoreService evalScoreService;

    @Resource
    private EvalDimensionService evalDimensionService;

    @Resource
    private SysUserService sysUserService;

    @GetMapping("/student")
    public Result<?> getStudentScores(@RequestParam Long studentId,
                                      @RequestParam(required = false) Long courseId) {
        if (courseId != null) {
            List<EvalScore> list = evalScoreService.getStudentScores(studentId, courseId);
            return Result.success(list);
        }
        // When courseId is not provided, return all scores for the student
        LambdaQueryWrapper<EvalScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvalScore::getStudentId, studentId);
        wrapper.orderByAsc(EvalScore::getCourseId).orderByAsc(EvalScore::getDimensionId);
        List<EvalScore> list = evalScoreService.list(wrapper);
        return Result.success(list);
    }

    @GetMapping("/class")
    public Result<?> getClassScores(@RequestParam Long courseId,
                                    @RequestParam Long classId) {
        List<EvalScore> list = evalScoreService.getClassScores(courseId, classId);
        return Result.success(list);
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody EvalScore score) {
        boolean saved = evalScoreService.save(score);
        return saved ? Result.success() : Result.error("保存失败");
    }

    @PostMapping("/batch")
    public Result<?> saveBatch(@RequestBody List<EvalScore> scores) {
        boolean saved = evalScoreService.saveBatch(scores);
        return saved ? Result.success() : Result.error("批量保存失败");
    }

    @PostMapping("/import")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file,
                                 @RequestParam Long courseId,
                                 @RequestParam Long teacherId) {
        try {
            List<ScoreImportDTO> dataList = EasyExcel.read(file.getInputStream())
                    .head(ScoreImportDTO.class)
                    .sheet()
                    .doReadSync();

            // Get all dimensions
            List<EvalDimension> dimensions = evalDimensionService.list();
            Map<String, Long> dimensionMap = new HashMap<>();
            for (EvalDimension dim : dimensions) {
                dimensionMap.put(dim.getName(), dim.getId());
            }

            List<EvalScore> scoreList = new ArrayList<>();
            for (ScoreImportDTO dto : dataList) {
                // Find student by studentNo
                LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(SysUser::getStudentNo, dto.getStudentNo());
                SysUser student = sysUserService.getOne(userWrapper);
                if (student == null) {
                    continue;
                }

                // Create score records for each dimension
                if (dto.getAttendance() != null && dimensionMap.containsKey("考勤")) {
                    EvalScore score = new EvalScore();
                    score.setStudentId(student.getId());
                    score.setCourseId(courseId);
                    score.setTeacherId(teacherId);
                    score.setDimensionId(dimensionMap.get("考勤"));
                    score.setScore(BigDecimal.valueOf(dto.getAttendance()));
                    score.setEvalDate(new Date());
                    scoreList.add(score);
                }
                if (dto.getHomework() != null && dimensionMap.containsKey("作业")) {
                    EvalScore score = new EvalScore();
                    score.setStudentId(student.getId());
                    score.setCourseId(courseId);
                    score.setTeacherId(teacherId);
                    score.setDimensionId(dimensionMap.get("作业"));
                    score.setScore(BigDecimal.valueOf(dto.getHomework()));
                    score.setEvalDate(new Date());
                    scoreList.add(score);
                }
                if (dto.getExperiment() != null && dimensionMap.containsKey("实验")) {
                    EvalScore score = new EvalScore();
                    score.setStudentId(student.getId());
                    score.setCourseId(courseId);
                    score.setTeacherId(teacherId);
                    score.setDimensionId(dimensionMap.get("实验"));
                    score.setScore(BigDecimal.valueOf(dto.getExperiment()));
                    score.setEvalDate(new Date());
                    scoreList.add(score);
                }
                if (dto.getQuiz() != null && dimensionMap.containsKey("测验")) {
                    EvalScore score = new EvalScore();
                    score.setStudentId(student.getId());
                    score.setCourseId(courseId);
                    score.setTeacherId(teacherId);
                    score.setDimensionId(dimensionMap.get("测验"));
                    score.setScore(BigDecimal.valueOf(dto.getQuiz()));
                    score.setEvalDate(new Date());
                    scoreList.add(score);
                }
                if (dto.getParticipation() != null && dimensionMap.containsKey("课堂参与")) {
                    EvalScore score = new EvalScore();
                    score.setStudentId(student.getId());
                    score.setCourseId(courseId);
                    score.setTeacherId(teacherId);
                    score.setDimensionId(dimensionMap.get("课堂参与"));
                    score.setScore(BigDecimal.valueOf(dto.getParticipation()));
                    score.setEvalDate(new Date());
                    scoreList.add(score);
                }
            }

            if (!scoreList.isEmpty()) {
                evalScoreService.saveBatch(scoreList);
            }
            return Result.success("导入成功，共导入" + scoreList.size() + "条记录", null);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/weighted/{studentId}/{courseId}")
    public Result<?> getWeightedTotalScore(@PathVariable Long studentId,
                                           @PathVariable Long courseId) {
        BigDecimal total = evalScoreService.calculateWeightedScore(studentId, courseId);
        return Result.success(total);
    }
}
