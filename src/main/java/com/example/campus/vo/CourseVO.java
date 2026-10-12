package com.example.campus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseVO {
    private Long id;
    private String name;
    private String teacher;
    private BigDecimal credit;
    private Integer capacity;
}
