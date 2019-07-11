package admin.controller;


import admin.entity.TesseractGroup;
import admin.pojo.CommonResponseVO;
import admin.pojo.GroupVO;
import admin.pojo.PageVO;
import admin.service.ITesseractGroupService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author nickle
 * @since 2019-07-10
 */
@RestController
@RequestMapping("/tesseract-group")
@Validated
public class TesseractGroupController {
    @Autowired
    private ITesseractGroupService groupService;


    @RequestMapping("/allGroup")
    public CommonResponseVO allGroup() {
        return CommonResponseVO.success(groupService.list());
    }


    @RequestMapping("/groupList")
    public CommonResponseVO groupList(@NotNull @Min(1) Integer currentPage
            , @NotNull @Min(1) @Max(50) Integer pageSize, TesseractGroup condition,
                                      Long startCreateTime,
                                      Long endCreateTime) {
        IPage<TesseractGroup> groupIPage = groupService.listByPage(currentPage, pageSize
                , condition, startCreateTime, endCreateTime);
        GroupVO groupVO = new GroupVO();
        PageVO pageVO = new PageVO();
        pageVO.setCurrentPage(groupIPage.getCurrent());
        pageVO.setPageSize(groupIPage.getSize());
        pageVO.setTotal(groupIPage.getTotal());
        groupVO.setPageInfo(pageVO);
        groupVO.setGroupList(groupIPage.getRecords());
        return CommonResponseVO.success(groupVO);
    }

    @RequestMapping("/addGroup")
    public CommonResponseVO addGroup(@Validated @RequestBody TesseractGroup tesseractGroup) throws Exception {
        groupService.saveOrUpdateGroup(tesseractGroup);
        return CommonResponseVO.SUCCESS;
    }


    @RequestMapping("/deleteGroup")
    public CommonResponseVO deleteGroup(@NotNull Integer groupId) throws Exception {
        groupService.deleteGroup(groupId);
        return CommonResponseVO.SUCCESS;
    }
}
