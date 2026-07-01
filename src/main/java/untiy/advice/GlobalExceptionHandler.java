package untiy.advice;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import untiy.exception.EIException;
import untiy.utils.R;

@RestControllerAdvice  // 相当于@ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public R handleAccessDeniedException(AccessDeniedException e) {
        return R.error(403, e.getMessage() != null ? e.getMessage() : "权限不足");
    }

    // 处理自定义的业务异常
    @ExceptionHandler(EIException.class)
    public R handleEIException(EIException e) {
        // 可以从异常中获取code和msg，返回给前端
        return R.error(e.getCode(), e.getMsg());
    }

    // 处理其他所有未捕获的异常（兜底）
    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        // 记录日志...
        e.printStackTrace();
        return R.error(500, "系统繁忙，请稍后再试");
    }
}