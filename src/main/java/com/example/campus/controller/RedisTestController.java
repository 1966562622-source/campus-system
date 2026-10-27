package com.example.campus.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/redis")
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    public RedisTestController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/set")
    public String setValue() {
        redisTemplate.opsForValue().set("test:name", "campus-system");
        return "Redis 写入成功";
    }

    @GetMapping("/get")
    public String getValue() {
        String value = redisTemplate.opsForValue().get("test:name");
        return "Redis 读取结果：" + value;
    }
}
