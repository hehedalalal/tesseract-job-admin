package admin.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponseVO {
    public static final int SUCCESS_STATUS = 200;
    public static final int FAIL_STATUS = 500;
    public static final String DEFAULT_FAIL_MSG = "服务器异常";
    public static final CommonResponseVO FAIL = new CommonResponseVO(FAIL_STATUS, DEFAULT_FAIL_MSG);
    public static final CommonResponseVO SUCCESS = new CommonResponseVO(SUCCESS_STATUS, null);
    private Integer status;
    private Object body;

    public static CommonResponseVO fail(Object body) {
        return new CommonResponseVO(FAIL_STATUS, body);
    }

    public static CommonResponseVO fail(int stauts, Object body) {
        return new CommonResponseVO(stauts, body);
    }

    public static CommonResponseVO success(Object body) {
        return new CommonResponseVO(SUCCESS_STATUS, body);
    }
}
