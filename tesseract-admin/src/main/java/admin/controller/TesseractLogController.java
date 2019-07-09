package admin.controller;


import admin.entity.TesseractLog;
import admin.pojo.CommonResponseVO;
import admin.pojo.LogVO;
import admin.pojo.PageVO;
import admin.service.ITesseractLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tesseract.core.dto.TesseractAdminJobNotify;
import tesseract.core.dto.TesseractExecutorResponse;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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

    @RequestMapping("/logList")
    public CommonResponseVO logList(@NotNull @Min(1) Integer currentPage
            , @NotNull @Min(1) @Max(50) Integer pageSize
            , TesseractLog condition, Long startCreateTime,
                                    Long endCreateTime,
                                    Long startUpdateTime,
                                    Long endUpdateTime) {
        IPage<TesseractLog> logIPage = logService.listByPage(currentPage, pageSize, condition, startCreateTime,
                endCreateTime,
                startUpdateTime,
                endUpdateTime);
        LogVO logVO = new LogVO();
        PageVO pageVO = new PageVO();
        pageVO.setCurrentPage(logIPage.getCurrent());
        pageVO.setPageSize(logIPage.getSize());
        pageVO.setTotal(logIPage.getTotal());
        logVO.setPageInfo(pageVO);
        logVO.setLogList(logIPage.getRecords());
        return CommonResponseVO.success(logVO);
    }

    @RequestMapping("/getLogCount")
    private CommonResponseVO getLogCount() {
        return CommonResponseVO.success(logService.count());
    }

    /**
     * 统计最近七天的数据，线形图
     *
     * @return
     */
    @RequestMapping("/statisticsLogLine")
    private CommonResponseVO statisticsLogLine() {
        return CommonResponseVO.success(logService.statisticsLogLine());
    }

    /**
     * 统计饼图
     *
     * @return
     */
    @RequestMapping("/statisticsLogPie")
    private CommonResponseVO statisticsLogPie() {
        return CommonResponseVO.success(logService.statisticsLogPie());
    }

}
