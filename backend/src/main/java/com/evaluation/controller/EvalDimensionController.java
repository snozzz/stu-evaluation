package com.evaluation.controller;

import com.evaluation.entity.EvalDimension;
import com.evaluation.service.EvalDimensionService;
import com.evaluation.util.IdResetUtil;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/dimension")
public class EvalDimensionController {

    @Resource
    private EvalDimensionService evalDimensionService;

    @Resource
    private IdResetUtil idResetUtil;

    @GetMapping("/list")
    public Result<?> list() {
        List<EvalDimension> list = evalDimensionService.list();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody EvalDimension dimension) {
        boolean saved = evalDimensionService.save(dimension);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody EvalDimension dimension) {
        dimension.setId(id);
        boolean updated = evalDimensionService.updateById(dimension);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed = evalDimensionService.removeById(id);
        if (removed) {
            idResetUtil.resetAutoIncrement("eval_dimension");
            return Result.success();
        }
        return Result.error("删除失败");
    }
}
