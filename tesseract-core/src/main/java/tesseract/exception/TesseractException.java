package tesseract.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TesseractException extends RuntimeException {
    private String msg;

    public TesseractException(String msg, String cause) {
        super(cause);
        this.msg = msg;
    }
}
