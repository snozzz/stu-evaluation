package com.evaluation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluation.dto.LoginDTO;
import com.evaluation.entity.SysUser;

import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    Map<String, Object> login(LoginDTO dto);

    void register(SysUser user);

    void updateProfile(SysUser user);

    void changePassword(Long userId, String oldPwd, String newPwd);

    Page<SysUser> listByRole(String role, Page<SysUser> page);
}
