package com.example.campus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)throws ServletException, IOException{
        String authorizationHeader=request.getHeader("Authorization");
        if (authorizationHeader==null||!authorizationHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        String token = authorizationHeader.substring(7);

        try {
            // 验证 Token 并读取其中的声明
            Claims claims = jwtUtil.parseToken(token);

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            Long userId=claims.get("userId",Long.class);

            // 只有当前请求尚未认证时，才设置认证信息
            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(new LoginUser(userId,username),
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效、过期或格式错误
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write(
                    "{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}"
            );

            return;
        }

        // 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}
