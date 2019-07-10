package admin.controller;


import admin.entity.TesseractExecutor;
import admin.pojo.CommonResponseVO;
import admin.pojo.ExecutorVO;
import admin.service.ITesseractExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractAdminRegistryResDTO;
import tesseract.core.dto.TesseractExecutorResponse;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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

    @RequestMapping("/executorList")
    public CommonResponseVO executorList(@NotNull @Min(1) Long currentPage
            , @NotNull @Min(1) @Max(50) Long pageSize, TesseractExecutor condition,
                                         Long startCreateTime,
                                         Long endCreateTime) {
        ExecutorVO executorVO = tesseractExecutorService.listByPage(currentPage, pageSize
                , condition, startCreateTime, endCreateTime);
        return CommonResponseVO.success(executorVO);
    }


    @RequestMapping("/executorListNoDetail")
    public CommonResponseVO executorListNoDetail() {
        return CommonResponseVO.success(tesseractExecutorService.list());
    }

    @RequestMapping("/addExecutor")
    public CommonResponseVO addExecutor(@Validated @RequestBody TesseractExecutor tesseractExecutor) throws Exception {
        tesseractExecutorService.saveOrUpdateExecutor(tesseractExecutor);
        return CommonResponseVO.SUCCESS;
    }

    @RequestMapping("/deleteExecutor")
    public CommonResponseVO deleteExecutor(@NotNull Integer executorId) throws Exception {
        tesseractExecutorService.deleteExecutor(executorId);
        return CommonResponseVO.SUCCESS;
    }
}

