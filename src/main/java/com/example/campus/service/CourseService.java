package com.example.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.dto.CourseCreateDTO;
import com.example.campus.entity.Course;
import com.example.campus.exception.BusinessException;
import com.example.campus.mapper.CourseMapper;
import com.example.campus.vo.CourseVO;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {
    private final CourseMapper courseMapper;

    public CourseService(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }
    public CourseVO getById(Long id){
        Course course=courseMapper.selectById(id);
        if (course==null){
            throw new BusinessException("课程不存在");
        }
        CourseVO vo=new CourseVO();
        vo.setName(course.getName());
        vo.setId(course.getId());
        vo.setName(course.getName());
        vo.setTeacher(course.getTeacher());
        vo.setCredit(course.getCredit());
        vo.setCapacity(course.getCapacity());
        return vo;
    }
    public List<CourseVO> listCourses() {
        List<Course> courses=courseMapper.selectList(null);
        List<CourseVO> result=new ArrayList<>();
        for (Course course : courses){
            CourseVO vo=new CourseVO();
            vo.setId(course.getId());
            vo.setName(course.getName());
            vo.setTeacher(course.getTeacher());
            vo.setCredit(course.getCredit());
            vo.setCapacity(course.getCapacity());
            result.add(vo);
        }
        return result;
    }
    public IPage<CourseVO> listCoursesByPage(long current,long size,String name,String teacher){
        Page<Course> page=new Page<>(current,size);
        QueryWrapper<Course> wrapper=new QueryWrapper<>();
        if (name!=null&&!name.trim().isEmpty()){
            wrapper.like("name",name.trim());
        }
        if (teacher!=null&&!teacher.trim().isEmpty()){
            wrapper.eq("teacher",teacher.trim());
        }
        IPage<Course> coursePage=courseMapper.selectPage(page,wrapper);

        Page<CourseVO> voPage=new Page<>(current,size);
        voPage.setTotal(coursePage.getTotal());
        List<CourseVO> voList=new ArrayList<>();
        for (Course course:coursePage.getRecords()){
            CourseVO vo=new CourseVO();
            BeanUtils.copyProperties(course,vo);
            voList.add(vo);

        }
        voPage.setRecords(voList);
        return voPage;
    }


    public void update(Course course){
        courseMapper.updateById(course);
    }
    public void delete(Long id){
        courseMapper.deleteById(id);
    }
    public Course create(CourseCreateDTO dto){
        Course course=new Course();
        course.setName(dto.getName());
        course.setTeacher(dto.getTeacher());
        course.setCredit(dto.getCredit());
        course.setCapacity(dto.getCapacity());
        courseMapper.insert(course);
        return course;
    }

}
