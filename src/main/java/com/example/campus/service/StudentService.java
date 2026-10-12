package com.example.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.dto.SelectedCourseDTO;
import com.example.campus.entity.Student;
import com.example.campus.exception.BusinessException;
import com.example.campus.mapper.CourseMapper;
import com.example.campus.mapper.StudentMapper;
import com.example.campus.vo.CourseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    private final StudentMapper studentMapper;

    private final CourseMapper courseMapper;

    private final CourseService courseService;

    public StudentService(StudentMapper studentMapper, CourseMapper courseMapper, CourseService courseService) {
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
        this.courseService = courseService;
    }

    public Student select(Long id){
        return studentMapper.selectById(id);
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
    public void delete(Long id){
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
