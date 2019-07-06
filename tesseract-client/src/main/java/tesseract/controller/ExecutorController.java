package tesseract.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tesseract.core.constant.CommonConstant;
import tesseract.core.dto.TesseractExecutorRequest;
import tesseract.core.dto.TesseractExecutorResponse;
import tesseract.core.executor.TesseractExecutor;

@RestController
@ControllerAdvice
@Validated
public class ExecutorController {
    @Autowired
    private TesseractExecutor tesseractExecutor;

    @RequestMapping(CommonConstant.EXECUTE_MAPPING)
    public TesseractExecutorResponse executeJob(@Validated @RequestBody TesseractExecutorRequest tesseractExecutorRequest) {
        return tesseractExecutor.execute(tesseractExecutorRequest);
    }

    @ExceptionHandler(Throwable.class)
    public TesseractExecutorResponse exceptionHandler(Throwable throwable) {
        TesseractExecutorResponse fail = new TesseractExecutorResponse(TesseractExecutorResponse.FAIL_STAUTS, throwable.getMessage());
        return fail;
    }
}
