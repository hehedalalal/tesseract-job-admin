package admin.pojo;

import lombok.Data;

@Data
public class CommonResponseVO {
    public static final int SUCCESS_STATUS = 200;
    public static final int FAIL_STATUS = 500;
    public static final String DEFAULT_FAIL_MSG = "服务器异常";
    public static final CommonResponseVO FAIL = new CommonResponseVO(FAIL_STATUS, DEFAULT_FAIL_MSG, null);
    public static final CommonResponseVO SUCCESS = new CommonResponseVO(SUCCESS_STATUS, null, null);
    private Integer status;
    private String msg;
    private Object body;

    public CommonResponseVO(Integer status, String msg, Object body) {
        this.status = status;
        this.msg = msg;
        this.body = body;
    }

    public static CommonResponseVO fail(Object body) {
        return fail(FAIL_STATUS, body);
    }

    public static CommonResponseVO fail(int status, Object body) {
        return fail(status, null, body);
    }

    public static CommonResponseVO fail(int status, String msg, Object body) {
        return new CommonResponseVO(status, msg, body);
    }


    public static CommonResponseVO success(Object body) {
        return fail(SUCCESS_STATUS, body);
    }

    public static CommonResponseVO success(int status, Object body) {
        return fail(status, null, body);
    }

    public static CommonResponseVO success(int status, String msg, Object body) {
        return new CommonResponseVO(status, msg, body);
    }
}
