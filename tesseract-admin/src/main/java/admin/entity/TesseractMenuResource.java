package admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
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
@ApiModel(value="TesseractMenuResource对象", description="")
public class TesseractMenuResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键，自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "菜单名称")
    private String name;

    @ApiModelProperty(value = "前端组件名称")
    private String component;

    @ApiModelProperty(value = "菜单路由地址")
    private String path;

    @ApiModelProperty(value = "路径匹配模式")
    private String urlPattern;

    @ApiModelProperty(value = "菜单的图标")
    @TableField("iconCls")
    private String iconCls;

    @ApiModelProperty(value = "父级菜单ID")
    private Integer parentId;

    @ApiModelProperty(value = "菜单描述")
    private String desc;

    @ApiModelProperty(value = "菜单顺序")
    private Integer order;

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
