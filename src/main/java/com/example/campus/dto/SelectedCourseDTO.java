package com.example.campus.dto;

import lombok.Data;

@Data
public class SelectedCourseDTO {
    private Long id;
    private String name;
    private Integer capacity;
    private Integer selectedCount;
}
