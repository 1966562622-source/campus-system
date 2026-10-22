package com.example.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.dto.SelectedCourseDTO;
import com.example.campus.entity.Student;
import com.example.campus.entity.StudentCourse;
import com.example.campus.exception.BusinessException;
import com.example.campus.mapper.CourseMapper;
import com.example.campus.mapper.StudentCourseMapper;
import com.example.campus.mapper.StudentMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {
    private final StudentMapper studentMapper;

    private final CourseMapper courseMapper;

    private final CourseService courseService;

    private final StudentCourseMapper studentCourseMapper;

    public StudentService(StudentMapper studentMapper, CourseMapper courseMapper, CourseService courseService, StudentCourseMapper studentCourseMapper) {
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
        this.courseService = courseService;
        this.studentCourseMapper = studentCourseMapper;
    }

    public Student select(Long id){

        Student student= studentMapper.selectById(id);
        if (student==null){
            throw new BusinessException(HttpStatus.NOT_FOUND,"学生不存在");
        }
        return student;
    }

    public List<Student> list(){
        return studentMapper.selectList(null);
    }

    public List<Student> getStudentsByCourseId(Long courseLd){
        courseService.getById(courseLd);
        return studentMapper.selectstudentsByCourseId(courseLd);
    }

    public IPage<Student> getStudentsByCourseIdPage(Long courseId,long current,long size){
        courseService.getById(courseId);
        Page<Student> page=new Page<>(current,size);
        return studentMapper.selectStudentsByCourseIdPage(page,courseId);
    }

    public Student create(Student student){
        studentMapper.insert(student);
        return student;
    }
    public void update(Student student){
        studentMapper.updateById(student);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Student student=studentMapper.selectById(id);
        if (student==null){
            throw new BusinessException(HttpStatus.NOT_FOUND,"学生不存在");
        }
        studentCourseMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId,id)
        );
        studentMapper.deleteById(id);
    }

    public List<SelectedCourseDTO> getSelectedCourses(Long studentId){
        Student student=studentMapper.selectById(studentId);
        if (student==null){
            throw new BusinessException("学生不存在");
        }
        return courseMapper.selectCoursesByStudentId(studentId);
    }

}
