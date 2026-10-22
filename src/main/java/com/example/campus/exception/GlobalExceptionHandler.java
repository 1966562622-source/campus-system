package com.example.campus.exception;

import com.example.campus.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //参数校验异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleValidationException(MethodArgumentNotValidException e){
        String message =e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400,message));
    }
    //
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e){
        int code=e.getStatus().value();
        return ResponseEntity
                .status(e.getStatus())
                .body( Result.error(code,e.getMessage()));

    }
//    修改约束校验异常处理方法
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<?>> handleConstraintViolationException(ConstraintViolationException e){
        String message=e.getConstraintViolations()
                .stream()
                .map(violation->violation.getMessage())
                .findFirst()
                .orElse("请求参数不合法");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400,message));
    }
//    修改系统异常处理方法
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e){
        log.error("系统发生未处理异常",e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500,"系统繁忙，请稍后重试"));
    }
//    认证异常处理方法
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Result<?>> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException e
    ){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(401,"用户名或密码错误"));
    }

}
