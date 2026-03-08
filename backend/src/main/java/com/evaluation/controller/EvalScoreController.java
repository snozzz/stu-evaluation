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

            if (dataList == null || dataList.isEmpty()) {
                return Result.error("Excel文件为空，请检查文件内容");
            }

            // Get all dimensions
            List<EvalDimension> dimensions = evalDimensionService.list();
            Map<String, Long> dimensionMap = new HashMap<>();
            for (EvalDimension dim : dimensions) {
                dimensionMap.put(dim.getName(), dim.getId());
            }

            int importCount = 0;
            int updateCount = 0;
            List<String> skippedStudents = new ArrayList<>();

            for (ScoreImportDTO dto : dataList) {
                if (dto.getStudentNo() == null || dto.getStudentNo().trim().isEmpty()) {
                    continue;
                }
                // Find student by studentNo
                LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(SysUser::getStudentNo, dto.getStudentNo().trim());
                SysUser student = sysUserService.getOne(userWrapper);
                if (student == null) {
                    skippedStudents.add(dto.getStudentNo());
                    continue;
                }

                // Map dimension name -> score value
                Map<String, Double> dimScores = new LinkedHashMap<>();
                dimScores.put("考勤", dto.getAttendance());
                dimScores.put("作业", dto.getHomework());
                dimScores.put("实验", dto.getExperiment());
                dimScores.put("测验", dto.getQuiz());
                dimScores.put("课堂参与", dto.getParticipation());

                for (Map.Entry<String, Double> entry : dimScores.entrySet()) {
                    if (entry.getValue() == null || !dimensionMap.containsKey(entry.getKey())) {
                        continue;
                    }
                    Long dimensionId = dimensionMap.get(entry.getKey());

                    // Check if score already exists (avoid duplicates)
                    LambdaQueryWrapper<EvalScore> existWrapper = new LambdaQueryWrapper<>();
                    existWrapper.eq(EvalScore::getStudentId, student.getId())
                            .eq(EvalScore::getCourseId, courseId)
                            .eq(EvalScore::getDimensionId, dimensionId);
                    EvalScore existing = evalScoreService.getOne(existWrapper);

                    if (existing != null) {
                        // Update existing score
                        existing.setScore(BigDecimal.valueOf(entry.getValue()));
                        existing.setTeacherId(teacherId);
                        existing.setEvalDate(new Date());
                        evalScoreService.updateById(existing);
                        updateCount++;
                    } else {
                        // Insert new score
                        EvalScore score = new EvalScore();
                        score.setStudentId(student.getId());
                        score.setCourseId(courseId);
                        score.setTeacherId(teacherId);
                        score.setDimensionId(dimensionId);
                        score.setScore(BigDecimal.valueOf(entry.getValue()));
                        score.setEvalDate(new Date());
                        evalScoreService.save(score);
                        importCount++;
                    }
                }
            }

            StringBuilder msg = new StringBuilder();
            msg.append("导入完成：新增").append(importCount).append("条，更新").append(updateCount).append("条");
            if (!skippedStudents.isEmpty()) {
                msg.append("。未找到学号：").append(String.join("、", skippedStudents));
            }
            return Result.success(msg.toString(), null);
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
