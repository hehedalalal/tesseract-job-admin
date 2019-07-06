package admin.controller;


import admin.service.ITesseractLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import tesseract.core.dto.TesseractAdminJobNotify;
import tesseract.core.dto.TesseractExecutorResponse;

import static tesseract.core.constant.CommonConstant.NOTIFY_MAPPING_SUFFIX;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */

@RestController
@RequestMapping("/tesseract-log")
public class TesseractLogController {
    @Autowired
    public ITesseractLogService logService;

    @RequestMapping(NOTIFY_MAPPING_SUFFIX)
    private TesseractExecutorResponse notify(@Validated @RequestBody TesseractAdminJobNotify tesseractAdminJobNotify) {
        logService.notify(tesseractAdminJobNotify);
        return TesseractExecutorResponse.SUCCESS;
    }
}
