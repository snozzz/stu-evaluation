package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluation.entity.Announcement;
import com.evaluation.service.AnnouncementService;
import com.evaluation.util.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime);
        Page<Announcement> result = announcementService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Announcement announcement) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        announcement.setPublisherId(userId);
        boolean saved = announcementService.save(announcement);
        return saved ? Result.success() : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        boolean updated = announcementService.updateById(announcement);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed = announcementService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }
}
