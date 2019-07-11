package tesseract.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TesseractHeartbeatRequest {
    @NotBlank
    private String socket;
    @NotNull
    Integer activeCount;
    @NotNull
    Integer corePoolSize;
    @NotNull
    Integer maximumPoolSize;
    @NotNull
    Integer poolSize;
    @NotNull
    Integer queueSize;
}
