package com.example.campus.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
@Data
public class CourseCreateDTO {
    @NotBlank(message = "课程名称不能为空")
    private String name;
    @NotBlank(message = "教师姓名不能为空")
    private String teacher;
    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.5",message = "学分必须大于等于0.5")
    private BigDecimal credit;
    @NotNull(message = "课程容量不能为空")
    @Min(value = 1,message = "课程容量必须大于0")
    private Integer capacity;
}
