package admin.advice;

import admin.pojo.CommonResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tesseract.exception.TesseractException;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionAdvice {


    @ExceptionHandler(TesseractException.class)
    public CommonResponseVO tesseractExceptionExceptionHandler(TesseractException e) {
        log.error(e.getMsg());
        return CommonResponseVO.fail(e.getStatus(), e.getMsg());
    }

    @ExceptionHandler(Exception.class)
    public CommonResponseVO commonExceptionHandler(Exception e) {
        log.error(e.getMessage());
        return CommonResponseVO.FAIL;
    }
}
