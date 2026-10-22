package com.example.campus;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        String rawPassword="bangbangda";
        String encodedPassword=encoder.encode(rawPassword);
        System.out.println(encodedPassword);
    }
}
