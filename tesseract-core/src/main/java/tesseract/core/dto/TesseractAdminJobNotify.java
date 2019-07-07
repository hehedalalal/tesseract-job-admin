package tesseract.core.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 〈执行器执行Job情况〉
 *
 * @author nickel
 * @create 2019/7/6
 * @since 1.0.0
 */
@Data
public class TesseractAdminJobNotify {
    @NotNull
    private Long logId;
    @NotNull
    private Integer triggerId;
    @NotNull
    private Integer executorId;
    private String exception;
//    private String log;
}
