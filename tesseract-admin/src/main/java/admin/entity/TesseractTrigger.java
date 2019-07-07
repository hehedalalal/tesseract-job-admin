package admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static admin.constant.AdminConstant.TRGGER_STATUS_STOPING;

/**
 * <p>
 *
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TesseractTrigger对象", description = "")
public class TesseractTrigger implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotBlank
    private String name;

    private Long nextTriggerTime;

    private Long prevTriggerTime;

    @NotBlank
    private String cron;
    @NotNull
    private Integer strategy;
    @NotNull
    private Integer shardingNum;
    @NotNull
    private Integer retryCount;
    @NotBlank
    private String description;

    private Integer status;

    @NotNull
    private Integer executorId;

    @NotBlank
    private String executor_name;

    private String creator = "admin";

    private Long createTime;

    private Long updateTime;


}
