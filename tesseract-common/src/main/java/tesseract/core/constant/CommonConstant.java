package tesseract.core.constant;

public class CommonConstant {
    /**
     * 请求地址
     */
    public static final String EXECUTE_MAPPING = "/execute";
    public static final String REGISTRY_MAPPING_SUFFIX = "/registry";
    public static final String REGISTRY_MAPPING = "/tesseract-executor" + REGISTRY_MAPPING_SUFFIX;

    public static final String NOTIFY_MAPPING_SUFFIX = "/notify";
    public static final String NOTIFY_MAPPING = "/tesseract-log" + NOTIFY_MAPPING_SUFFIX;

    public static final String HEARTBEAT_MAPPING_SUFFIX = "/heartBeat";
    public static final String HEARTBEAT_MAPPING = "/tesseract-executor-detail" + HEARTBEAT_MAPPING_SUFFIX;

    /**
     * 状态码
     */
    public static final Integer REGISTRY_REPEAT = 400;
    public static final Integer EXECUTOR_DETAIL_NOT_FIND = 501;
    /**
     * http
     */
    public static final String HTTP_PREFIX = "http://";

    /**
     * formatter
     */
    public static final String SOCKET_FORMATTER = "%s:%d";
}
