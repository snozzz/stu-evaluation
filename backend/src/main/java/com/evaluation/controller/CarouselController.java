package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluation.entity.Carousel;
import com.evaluation.service.CarouselService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/carousel")
public class CarouselController {

    @Resource
    private CarouselService carouselService;

    @GetMapping("/list")
    public Result<?> list() {
        LambdaQueryWrapper<Carousel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Carousel::getSortOrder);
        List<Carousel> list = carouselService.list(wrapper);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Carousel carousel) {
        boolean saved = carouselService.save(carousel);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Carousel carousel) {
        carousel.setId(id);
        boolean updated = carouselService.updateById(carousel);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed = carouselService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }
}
