package com.example.campus.service;

import com.example.campus.dto.LoginRequest;
import com.example.campus.entity.SysUser;
import com.example.campus.mapper.SysUserMapper;
import com.example.campus.security.JwtUtil;
import com.example.campus.vo.LoginVo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public SysUser authenticate(LoginRequest request){
        SysUser user=sysUserMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername,request.getUsername()));
        if (user==null){
            throw new org.springframework.security.authentication.BadCredentialsException("用户名或密码错误");
        }
        boolean passwordMatches=passwordEncoder.matches(request.getPassword(),
                user.getPassword());
        if (!passwordMatches){
            throw new org.springframework.security.authentication.BadCredentialsException("用户名或密码错误");
        }
        return user;
    }

    public LoginVo login(LoginRequest request){
        SysUser user=authenticate(request);
        String token= jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
        return new LoginVo(token,user.getId(),user.getUsername(),user.getRole());
    }

    public SysUser getUserById(Long userId){
        SysUser user=sysUserMapper.selectById(userId);
        if (user==null){
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("用户不存在");
        }
        return user;
    }
}
