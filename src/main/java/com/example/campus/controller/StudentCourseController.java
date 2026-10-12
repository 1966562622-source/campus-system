package com.example.campus.controller;

import com.example.campus.common.Result;
import com.example.campus.exception.BusinessException;
import com.example.campus.service.StudentCourseService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/students")
public class StudentCourseController {
    private final StudentCourseService studentCourseService;

    public StudentCourseController(StudentCourseService studentCourseService) {
        this.studentCourseService = studentCourseService;
    }
    @PostMapping("/{studentId}/courses/{courseId}")
    public Result<?> selectCourse(
            @PathVariable
            @Min(value = 1,message = "学生ID必须大于0")
            Long studentId,
            @PathVariable
            @Min(value = 1,message = "课程ID必须大于0")
            Long courseId
            ){
        studentCourseService.selectCourse(studentId,courseId);

        return Result.success();

    }

    @DeleteMapping("/{studentId}/courses/{courseId}")
    public Result<?> cancelCourse(
            @PathVariable
            @Min(value = 1,message = "学生ID必须大于0")
            Long studentId,
            @PathVariable
            @Min(value = 1,message = "课程ID必须大于0")
            Long courseId
            ){
        studentCourseService.cancelCourse(studentId,courseId);
        return Result.success("取消选课成功");
    }
}
