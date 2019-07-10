package admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author nickle
 * @since 2019-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TesseractBtnResource对象", description="")
public class TesseractBtnResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键，自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "按钮编码（用于界面配置）")
    private String btnCode;

    @ApiModelProperty(value = "按钮权限标志")
    private String btnAuthCode;

    @ApiModelProperty(value = "按钮名称")
    private String btnName;

    @ApiModelProperty(value = "所属菜单ID")
    private Integer menuId;

    @ApiModelProperty(value = "创建人ID")
    private Integer createUserId;

    @ApiModelProperty(value = "创建人姓名")
    private String createUserName;

    @ApiModelProperty(value = "更新人ID")
    private Integer updateUserId;

    @ApiModelProperty(value = "更新人姓名")
    private String updateUserName;

    @ApiModelProperty(value = "是否删除，0-未删除，1-删除")
    private Integer isDel;

    @ApiModelProperty(value = "状态码，保留字段")
    private Integer status;


}
