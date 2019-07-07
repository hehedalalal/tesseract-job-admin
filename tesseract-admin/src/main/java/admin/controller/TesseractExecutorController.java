package admin.controller;


import admin.entity.TesseractExecutor;
import admin.entity.TesseractTrigger;
import admin.pojo.*;
import admin.service.ITesseractExecutorService;
import admin.service.ITesseractTriggerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractAdminRegistryResDTO;
import tesseract.core.dto.TesseractExecutorResponse;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.util.List;

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
            , @NotNull @Min(1) @Max(50) Long pageSize, TesseractExecutor condition) {
        ExecutorVO executorVO = tesseractExecutorService.listByPage(currentPage, pageSize, condition);
        return CommonResponseVO.success(executorVO);
    }


    @RequestMapping("/executorListNoDetail")
    public CommonResponseVO executorListNoDetail() {
        return CommonResponseVO.success(tesseractExecutorService.list());
    }

    @RequestMapping("/addExecutor")
    public CommonResponseVO addExecutor(@Validated @RequestBody TesseractExecutor tesseractExecutor) throws Exception {
        tesseractExecutorService.saveExecutor(tesseractExecutor);
        return CommonResponseVO.SUCCESS;
    }
}

