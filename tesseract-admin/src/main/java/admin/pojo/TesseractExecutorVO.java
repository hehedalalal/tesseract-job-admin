package admin.pojo;

import admin.entity.TesseractExecutor;
import admin.entity.TesseractExecutorDetail;
import lombok.Data;

import java.util.List;

/**
 * 〈〉
 *
 * @author nickel
 * @create 2019/7/7
 * @since 1.0.0
 */
@Data
public class TesseractExecutorVO {
    private TesseractExecutor executor;
    private List<TesseractExecutorDetail> executorDetailList;
}
