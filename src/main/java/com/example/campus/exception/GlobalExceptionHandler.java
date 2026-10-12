package com.example.campus.exception;

import com.example.campus.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e){
        String message =e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return Result.error(400,message);
    }
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e){
        return Result.error(400,e.getMessage());
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e){
        String message=e.getConstraintViolations()
                .stream()
                .map(violation->violation.getMessage())
                .findFirst()
                .orElse("请求参数不合法");
        return Result.error(400,message);
    }
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e){
        log.error("系统发生未处理异常",e);
        return Result.error(500,"系统繁忙，请稍后重试");
    }

}
