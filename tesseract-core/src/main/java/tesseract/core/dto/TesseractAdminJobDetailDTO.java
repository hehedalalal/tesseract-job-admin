package tesseract.core.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TesseractAdminJobDetailDTO {
    @NotBlank
    private String className;
    @NotBlank
    private String triggerName;
}
