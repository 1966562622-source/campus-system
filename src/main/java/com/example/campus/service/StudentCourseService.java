package com.example.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.campus.entity.Course;
import com.example.campus.entity.Student;
import com.example.campus.entity.StudentCourse;
import com.example.campus.exception.BusinessException;
import com.example.campus.mapper.CourseMapper;
import com.example.campus.mapper.StudentCourseMapper;
import com.example.campus.mapper.StudentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class StudentCourseService {
    private final StudentMapper studentMapper;
    private  final CourseMapper courseMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final StringRedisTemplate redisTemplate;

    public StudentCourseService(StudentMapper studentMapper, CourseMapper courseMapper, StudentCourseMapper studentCourseMapper, StringRedisTemplate redisTemplate) {
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
        this.studentCourseMapper = studentCourseMapper;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public  void selectCourse(Long studentId,Long courseId){

        log.info("收到选课请求:studentId={},courseId={}",studentId,courseId);
        log.info("准备获取课程锁:studentId={},courseId={}",studentId,courseId);
        Course course=courseMapper.selectByIdForUpdate(courseId);
        if (course==null){
            throw new BusinessException("课程不存在");
        }
        log.info("已获取课程锁，studentId={},courseId={}",studentId,courseId);
        Student student=studentMapper.selectById(studentId);
        if (student==null){
            throw new BusinessException("学生不存在");
        }
        LambdaQueryWrapper<StudentCourse> duplicateWrapper=new LambdaQueryWrapper<>();
        duplicateWrapper.eq(StudentCourse::getStudentId,studentId)
                .eq(StudentCourse::getCourseId,courseId);
        Long duplicateCount=studentCourseMapper.selectCount(duplicateWrapper);
        if (duplicateCount>0){
            throw new BusinessException("不能重复选课");
        }
        LambdaQueryWrapper<StudentCourse> countWrapper=new LambdaQueryWrapper<>();
        countWrapper.eq(StudentCourse::getCourseId,courseId);
        Long selectedCount=studentCourseMapper.selectCount(countWrapper);

        log.info("选课人数检查:studentId={},courseId={},selectedCount={},capacity={}",studentId,courseId,selectedCount,course.getCapacity());
        if (selectedCount>=course.getCapacity()){
            throw new BusinessException("课程已满员");
        }
        StudentCourse studentCourse=new StudentCourse();
        studentCourse.setStudentId(studentId);
        studentCourse.setCourseId(courseId);
        studentCourseMapper.insert(studentCourse);
        redisTemplate.delete("course:"+courseId+":students");
        log.info("学生选课成功，studentId={},courseId={}",studentId,courseId);


    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelCourse(Long studentId,Long courseId){
        log.info("收到取消选课请求：studentId={},courseId={}",studentId,courseId);
        Student student=studentMapper.selectById(studentId);
        if (student==null){
            throw new BusinessException("学生不存在");
        }
        Course course=courseMapper.selectById(courseId);
        if (course==null){
            throw new BusinessException("课程不存在");
        }
        LambdaQueryWrapper<StudentCourse> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getStudentId,studentId)
                .eq(StudentCourse::getCourseId,courseId);
        StudentCourse studentCourse=studentCourseMapper.selectOne(wrapper);
        if (studentCourse==null){
            throw new BusinessException("该学生尚未选择这门课程，无法取消");
        }
        studentCourseMapper.deleteById(studentCourse.getId());

        redisTemplate.delete("course:"+courseId+":students");
        log.info("学生取消选课成功:studentId={},courseId={}",studentId,courseId);
    }

}
