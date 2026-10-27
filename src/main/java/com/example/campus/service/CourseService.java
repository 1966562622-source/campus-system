package com.example.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.dto.CourseCreateDTO;
import com.example.campus.entity.Course;
import com.example.campus.exception.BusinessException;
import com.example.campus.mapper.CourseMapper;
import com.example.campus.vo.CourseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CourseService {
    private final CourseMapper courseMapper;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public CourseService(CourseMapper courseMapper, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.courseMapper = courseMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
    private void unlock(String lockKey, String lockValue) {
        redisTemplate.execute(
                new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class),
                Collections.singletonList(lockKey),
                lockValue
        );
    }

    public CourseVO getById(Long id) {

        String cacheKey = "course:" + id;

        String lockKey = "lock:course:" + id;

        CourseVO cachedCourse = getCachedCourse(cacheKey, id);
        if (cachedCourse != null) {
            return cachedCourse;
        }

        String lockValue = UUID.randomUUID().toString();

        Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                lockKey,
                lockValue,
                10,
                TimeUnit.SECONDS
        );

        if (Boolean.TRUE.equals(locked)) {
            log.info("成功获取课程缓存重建锁，courseId={}", id);
            try {
                CourseVO cachedAgain = getCachedCourse(cacheKey, id);
                if (cachedAgain != null) {
                    return cachedAgain;
                }
                Course course = courseMapper.selectById(id);
                if (course == null) {
                    log.info("课程不存在，准备写入空值缓存，courseId={}", id);
                    redisTemplate.opsForValue().set(
                            cacheKey,
                            "__NULL__",
                            60,
                            TimeUnit.SECONDS
                    );
                    throw new BusinessException("课程不存在");

                }
                CourseVO vo = new CourseVO();
                vo.setName(course.getName());
                vo.setId(course.getId());
                vo.setTeacher(course.getTeacher());
                vo.setCredit(course.getCredit());
                vo.setCapacity(course.getCapacity());

                String json = objectMapper.writeValueAsString(vo);

                long expireTime=600+new Random().nextInt(300);

                redisTemplate.opsForValue().set(
                        cacheKey,
                        json,
                        expireTime,
                        TimeUnit.SECONDS
                );
                return vo;
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("查询或重建课程缓存失败，courseId={}", id, e);
                throw new BusinessException("查询课程失败");
            } finally {
                try {
                    unlock(lockKey, lockValue);
                } catch (Exception e) {
                    log.warn("释放课程缓存重建锁失败，courseId={}", id, e);
                }
            }
        } else {
            log.info("未获取到课程缓存重建锁，courseId={}", id);
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待课程缓存时被中断", e);
                }
                CourseVO cachedAgain = getCachedCourse(cacheKey, id);
                if (cachedAgain != null) {
                    return cachedAgain;
                }
            }
            throw new RuntimeException("课程缓存正在重建，请稍后重试");
        }
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

        wrapper.orderByAsc("id");
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
//        String cacheKey="course:"+course.getId();
//
//        redisTemplate.delete(cacheKey);
//        int rows=courseMapper.updateById(course);
//        if (rows==0){
//            throw new BusinessException("课程不存在或未发生更新");
//        }
//
//        CompletableFuture.runAsync(()->{
//            try {
//                Thread.sleep(500);
//                redisTemplate.delete(cacheKey);
//                log.info("延迟双删完成，courseId={}",course.getId());
//            }catch (InterruptedException e){
//                Thread.currentThread().interrupt();
//                log.error("延迟双删被中断，courseId={}",course.getId(),e);
//            }catch (Exception e){
//                log.error("延迟删除课程缓存失败,courseId={}",course.getId(),e);
//            }
//        });

        Long id= course.getId();
        String lockKey="lock:course:"+id;
        String lockValue=UUID.randomUUID().toString();
        String cacheKey="course:"+id;
        Boolean locked=redisTemplate.opsForValue().setIfAbsent(lockKey,lockValue,10,TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)){
            throw new BusinessException("课程正在处理中，请稍后重试");
        }
        try {
            int rows=courseMapper.updateById(course);
            if (rows==0){
                throw new BusinessException("课程不存在或未发生更新");
            }
            redisTemplate.delete(cacheKey);
            log.info("课程更新成功并删除缓存，courseId={}",id);
        }finally {
            unlock(lockKey,lockValue);
        }

    }
    public void delete(Long id){
        courseMapper.deleteById(id);

        String cacheKey="course:"+id;
        redisTemplate.delete(cacheKey);
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

    private CourseVO getCachedCourse(String cacheKey, Long id) {
        try {
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);

            if (cachedValue == null) {
                return null;
            }

            log.info("课程缓存命中，courseId={}", id);

            if ("__NULL__".equals(cachedValue)) {
                throw new BusinessException("课程不存在");
            }

            return objectMapper.readValue(cachedValue, CourseVO.class);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("读取课程缓存失败，courseId={}", id, e);
            return null;
        }

    }

}
