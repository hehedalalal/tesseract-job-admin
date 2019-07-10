package admin.service.impl;

import admin.core.scheduler.TesseractScheduleBoot;
import admin.entity.TesseractGroup;
import admin.mapper.TesseractGroupMapper;
import admin.service.ITesseractGroupService;
import admin.util.AdminUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tesseract.exception.TesseractException;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-10
 */
@Service
public class TesseractGroupServiceImpl extends ServiceImpl<TesseractGroupMapper, TesseractGroup> implements ITesseractGroupService {

    @Override
    public void deleteGroup(Integer groupId) {
        TesseractGroup user = getById(groupId);
        if (user == null) {
            throw new TesseractException("用户不存在");
        }
        removeById(groupId);
    }

    @Override
    public void saveOrUpdateGroup(TesseractGroup tesseractGroup) {
        long currentTimeMillis = System.currentTimeMillis();
        Integer id = tesseractGroup.getId();
        if (id != null) {
            TesseractGroup oldGroup = getById(id);
            if (oldGroup == null) {
                throw new TesseractException("TesseractGroup为空");
            }
            Integer oldThreadPoolNum = oldGroup.getThreadPoolNum();
            Integer newThreadPoolNum = tesseractGroup.getThreadPoolNum();
            if (newThreadPoolNum <= 0) {
                throw new TesseractException("线程数不能为小于等于0");
            }
            if (!oldThreadPoolNum.equals(newThreadPoolNum)) {
                //更新线程数
                TesseractScheduleBoot.updateThreadNum(tesseractGroup.getName(), newThreadPoolNum);
            }
            tesseractGroup.setUpdateTime(currentTimeMillis);
            updateById(tesseractGroup);
            return;
        }
        tesseractGroup.setCreateTime(currentTimeMillis);
        tesseractGroup.setUpdateTime(currentTimeMillis);
        this.save(tesseractGroup);
    }

    @Override
    public IPage<TesseractGroup> listByPage(Integer currentPage, Integer pageSize, TesseractGroup condition, Long startCreateTime, Long endCreateTime) {
        Page<TesseractGroup> page = new Page<>(currentPage, pageSize);
        QueryWrapper<TesseractGroup> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<TesseractGroup> lambda = queryWrapper.lambda();
        //日期
        if (startCreateTime != null) {
            lambda.ge(TesseractGroup::getCreateTime, startCreateTime);
        }

        if (endCreateTime != null) {
            lambda.le(TesseractGroup::getCreateTime, endCreateTime);
        }

        //其他
        AdminUtils.buildCondition(queryWrapper, condition);
        return page(page, queryWrapper);
    }
}
