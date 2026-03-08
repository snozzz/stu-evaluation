package com.evaluation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluation.entity.ClassInfo;
import com.evaluation.entity.StudentClass;
import com.evaluation.entity.SysUser;
import com.evaluation.service.ClassInfoService;
import com.evaluation.service.StudentClassService;
import com.evaluation.service.SysUserService;
import com.evaluation.util.IdResetUtil;
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

    @Resource
    private IdResetUtil idResetUtil;

    @Resource
    private ClassInfoService classInfoService;

    @Resource
    private StudentClassService studentClassService;

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
                    .or().like(SysUser::getStudentNo, keyword));
        }
        wrapper.orderByAsc(SysUser::getId);
        Page<SysUser> result = sysUserService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody SysUser user) {
        try {
            sysUserService.register(user);
            syncStudentClass(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            boolean removed = sysUserService.removeById(id);
            if (removed) {
                idResetUtil.resetAutoIncrement("sys_user");
                return Result.success();
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        boolean updated = sysUserService.updateById(user);
        if (updated) {
            // Re-read to get full user info (role, className)
            SysUser fullUser = sysUserService.getById(id);
            if (fullUser != null) {
                syncStudentClass(fullUser);
            }
        }
        return updated ? Result.success() : Result.error("更新失败");
    }

    /**
     * Sync student_class relationship based on user's className field.
     * When a STUDENT user has a className, find matching ClassInfo and create/update the link.
     */
    private void syncStudentClass(SysUser user) {
        if (user == null || user.getId() == null) return;
        if (!"STUDENT".equals(user.getRole())) return;

        // Remove old student_class records for this student
        LambdaQueryWrapper<StudentClass> removeWrapper = new LambdaQueryWrapper<>();
        removeWrapper.eq(StudentClass::getStudentId, user.getId());
        studentClassService.remove(removeWrapper);

        // If className is set, find matching class and create link
        if (StringUtils.hasText(user.getClassName())) {
            LambdaQueryWrapper<ClassInfo> classWrapper = new LambdaQueryWrapper<>();
            classWrapper.eq(ClassInfo::getName, user.getClassName().trim());
            ClassInfo classInfo = classInfoService.getOne(classWrapper);
            if (classInfo != null) {
                StudentClass sc = new StudentClass();
                sc.setStudentId(user.getId());
                sc.setClassId(classInfo.getId());
                studentClassService.save(sc);
            }
        }
    }
}
