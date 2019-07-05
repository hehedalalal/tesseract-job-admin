package tesseract.core.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TesseractAdminRegistryRequest {
    @NotBlank
    private String ip;
    @NotNull
    private Integer port;
    @NotEmpty
    private List<TesseractAdminJobDetailDTO> tesseractAdminJobDetailDTOList;
}
