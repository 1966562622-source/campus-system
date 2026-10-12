package com.example.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.dto.SelectedCourseDTO;
import com.example.campus.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import javax.swing.*;
import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    @Select("SELECT * FROM course WHERE id=#{courseId} FOR UPDATE")
    Course selectByIdForUpdate(@Param("courseId")Long courseId);

    @Select("SELECT c.id,c.name,c.capacity, " +
            "(SELECT COUNT(*) FROM student_course sc2 "+
            "WHERE sc2.course_id=c.id) AS selectedCount "+
            "FROM student_course sc "+
            "JOIN course c ON sc.course_id=c.id " +
            "WHERE sc.student_id=#{studentId} " +
            "ORDER BY c.id")
    List<SelectedCourseDTO> selectCoursesByStudentId(@Param("studentId") Long id);
}
