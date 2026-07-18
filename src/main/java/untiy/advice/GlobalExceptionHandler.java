package untiy.advice;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import untiy.exception.EIException;
import untiy.utils.R;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public R handleAccessDeniedException(AccessDeniedException e) {
        return R.error(403, e.getMessage() != null ? e.getMessage() : "权限不足");
    }

    @ExceptionHandler(EIException.class)
    public R handleEIException(EIException e) {
        return R.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return R.error(400, "上传文件过大");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null && fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "请求参数校验失败";
        return R.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    public R handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null && fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "请求参数绑定失败";
        return R.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        e.printStackTrace();
        return R.error(500, "系统繁忙，请稍后再试");
    }
}
