package com.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluation.dto.LoginDTO;
import com.evaluation.entity.SysUser;
import com.evaluation.mapper.SysUserMapper;
import com.evaluation.service.SysUserService;
import com.evaluation.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", dto.getUsername());
        SysUser user = sysUserMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());
        userInfo.put("college", user.getCollege());
        userInfo.put("major", user.getMajor());
        userInfo.put("className", user.getClassName());
        userInfo.put("studentNo", user.getStudentNo());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        result.put("userInfo", userInfo);

        return result;
    }

    @Override
    public void register(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        sysUserMapper.insert(user);
    }

    @Override
    public void updateProfile(SysUser user) {
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setNickname(user.getNickname());
        updateUser.setAvatar(user.getAvatar());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());
        updateUser.setCollege(user.getCollege());
        updateUser.setMajor(user.getMajor());
        updateUser.setClassName(user.getClassName());
        updateUser.setRealName(user.getRealName());
        updateUser.setUpdateTime(new Date());
        sysUserMapper.updateById(updateUser);
    }

    @Override
    public void changePassword(Long userId, String oldPwd, String newPwd) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPwd));
        updateUser.setUpdateTime(new Date());
        sysUserMapper.updateById(updateUser);
    }

    @Override
    public Page<SysUser> listByRole(String role, Page<SysUser> page) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", role);
        return sysUserMapper.selectPage(page, queryWrapper);
    }
}
