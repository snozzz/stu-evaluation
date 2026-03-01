package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluation.entity.SysUser;
import com.evaluation.service.SysUserService;
import com.evaluation.util.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private SysUserService sysUserService;

    @GetMapping("/info")
    public Result<?> info() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody SysUser user) {
        try {
            sysUserService.updateProfile(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        try {
            sysUserService.changePassword(userId, oldPassword, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String role,
                          @RequestParam(required = false) String keyword) {
        Page<SysUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getUsername, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody SysUser user) {
        try {
            sysUserService.register(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean removed = sysUserService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        boolean updated = sysUserService.updateById(user);
        return updated ? Result.success() : Result.error("更新失败");
    }
}
