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
    public CommonResponseVO tesseractExceptionExeceptionHandler(TesseractException e) {
        log.error(e.getMsg());
        CommonResponseVO fail = CommonResponseVO.FAIL;
        fail.setMsg(e.getMsg());
        return fail;
    }

    @ExceptionHandler(Exception.class)
    public CommonResponseVO commonExeceptionHandler(Exception e) {
        log.error(e.getMessage());
        return CommonResponseVO.FAIL;
    }
}
