package com.example.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campus.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    @Select("SELECT s.*FROM student s "+
            "JOIN student_course sc ON s.id=sc.student_id "+
            "WHERE sc.course_id=#{courseId}"
    )
    List<Student> selectstudentsByCourseId(@Param("courseId")Long courseId);

    @Select("SELECT s.* FROM student s "+
            "JOIN student_course sc ON s.id=sc.student_id "+
            "WHERE sc.course_id=#{courseId}"
    )
    IPage<Student> selectStudentsByCourseIdPage(IPage<Student> page, @Param("courseId") Long courseId);
}
