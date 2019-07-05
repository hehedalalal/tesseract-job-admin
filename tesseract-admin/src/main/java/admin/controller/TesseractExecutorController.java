package admin.controller;


import admin.service.ITesseractExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractAdminRegistryResDTO;
import tesseract.core.dto.TesseractExecutorResponse;

import static tesseract.core.constant.CommonConstant.REGISTRY_MAPPING_SUFFIX;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
@RestController
@RequestMapping("/tesseract-executor")
@Validated
public class TesseractExecutorController {
    @Autowired
    private ITesseractExecutorService tesseractExecutorService;

    @RequestMapping(REGISTRY_MAPPING_SUFFIX)
    public TesseractExecutorResponse registry(@Validated @RequestBody TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws Exception {
        TesseractAdminRegistryResDTO registry = tesseractExecutorService.registry(tesseractAdminRegistryRequest);
        TesseractExecutorResponse success = new TesseractExecutorResponse(TesseractExecutorResponse.SUCCESS_STATUS, registry);
        return success;
    }
}

