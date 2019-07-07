package tesseract.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TesseractException extends RuntimeException {
    private String msg;
    private Integer status = 500;

    public TesseractException(Integer status, String msg) {
        super(msg);
        this.msg = msg;
        this.status = status;
    }

    public TesseractException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
