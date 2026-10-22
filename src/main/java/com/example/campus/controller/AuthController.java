package com.example.campus.controller;

import com.example.campus.entity.SysUser;
import com.example.campus.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.example.campus.common.Result;
import com.example.campus.dto.LoginRequest;
import com.example.campus.service.AuthService;
import com.example.campus.vo.LoginVo;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginRequest request){
        return Result.success(authService.login(request));
    }
    @GetMapping("/me")
    public Result<?> me(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser=(LoginUser) authentication.getPrincipal();

        SysUser user=authService.getUserById(loginUser.getUserId());

        String role=authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        return Result.success(java.util.Map.of("userId",user.getId(),"username",user.getUsername(),"role",role,"createdAt",user.getCreatedAt()));
    }
}
