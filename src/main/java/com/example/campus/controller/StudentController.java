package com.example.campus.controller;

import com.example.campus.dto.SelectedCourseDTO;
import com.example.campus.entity.Student;
import com.example.campus.entity.StudentCourse;
import com.example.campus.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{id}")
    public Student select(@PathVariable Long id){
        return studentService.select(id);
    }
    @GetMapping
    public List<Student> list(){
        return studentService.list();
    }
    @PostMapping
    public Student create(@RequestBody Student student){
        return studentService.create(student);
    }
    @PutMapping("/{id}")
    public Student update(@PathVariable Long id,@RequestBody Student student){
        student.setId(id);
        studentService.update(student);
        return student;
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        studentService.delete(id);
    }

    @GetMapping("/{studentId}/courses")
    public List<SelectedCourseDTO> getSelectCourses(@PathVariable Long studentId){
        return studentService.getSelectedCourses(studentId);
    }


}
