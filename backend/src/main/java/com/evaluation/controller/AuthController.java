package com.evaluation.controller;

import com.evaluation.dto.LoginDTO;
import com.evaluation.entity.SysUser;
import com.evaluation.service.SysUserService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> data = sysUserService.login(loginDTO);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody SysUser user) {
        try {
            sysUserService.register(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
