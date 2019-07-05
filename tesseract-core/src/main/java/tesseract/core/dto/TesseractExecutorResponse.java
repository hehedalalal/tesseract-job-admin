package tesseract.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TesseractExecutorResponse {
    public static final int SUCCESS_STATUS = 200;
    public static final String SUCCESS_MSG = "SUCCESS";
    public static final int FAIL_STAUTS = 500;
    public static final String FAIL_MSG = "FAILED";
    public static final TesseractExecutorResponse SUCCESS = new TesseractExecutorResponse(SUCCESS_STATUS, SUCCESS_MSG);
    public static final TesseractExecutorResponse FAIL = new TesseractExecutorResponse(FAIL_STAUTS, FAIL_MSG);
    private int status;
    private Object body;
}
