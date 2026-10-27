package com.example.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campus.common.Result;
import com.example.campus.dto.CourseCreateDTO;
import com.example.campus.entity.Course;
import com.example.campus.entity.Student;
import com.example.campus.service.CourseService;
import com.example.campus.service.StudentService;
import com.example.campus.vo.CourseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Validated
@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;
    private final StudentService studentService;

    public CourseController(CourseService courseService, StudentService studentService) {
        this.courseService = courseService;
        this.studentService = studentService;
    }

    @GetMapping("/{id}")
    public Result<CourseVO> getById(@PathVariable Long id){
        CourseVO courseVO=courseService.getById(id);
        return Result.success(courseVO);
    }
    @GetMapping
    public Result<List<CourseVO>> list(){
        List<CourseVO> courses= courseService.listCourses();
        return Result.success(courses);
    }
    @PostMapping
    public Result<Course> create(@Valid @RequestBody CourseCreateDTO dto){
        Course course=courseService.create(dto);
        return Result.success(course);
    }
    @PutMapping("/{id}")
    public void update(@PathVariable Long id,@RequestBody Course course){
        course.setId(id);
        courseService.update(course);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        courseService.delete(id);
    }

    @GetMapping("/page")
    public Result<IPage<CourseVO>> listCoursesBypage(
            @RequestParam(defaultValue = "1") @Min(value = 1,message = "当前页必须大于等于0")
            long current,
            @RequestParam(defaultValue = "10") @Min(value = 1,message = "每页数量不需大于0")
            long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String teacher){
        if (size>100){
            throw new IllegalArgumentException("每页最大查询100条记录");
        }
        return Result.success(courseService.listCoursesByPage(current,size,name,teacher));
    }
    @GetMapping("/{courseId}/students")
    public Result<List<Student>> getStudentByCourseId(
            @PathVariable
            @Min(value = 1,message = "课程ID必须大于0")
            Long courseId){
        List<Student> students=studentService.getStudentsByCourseId(courseId);
        return Result.success(students);
    }

    @GetMapping("/{courseId}/students/page")
    public Result<IPage<Student>> getStudentsByCourseIdPage(
            @PathVariable
            @Min(value = 1,message = "课程ID必须大于0")
            Long courseId,
            @RequestParam(defaultValue = "1")
            @Min(value = 1,message = "当前页必须大于0")
            long current,
            @RequestParam(defaultValue = "10")
            @Min(value = 1,message = "每页数量必须大于0")
            long size
            ){
        IPage<Student> page= studentService.getStudentsByCourseIdPage(courseId,current,size);
        return Result.success(page);
    }


}
