package tesseract.core.dto;

import lombok.Data;

import java.util.List;

@Data
public class TesseractAdminRegistryResDTO {
    private List<String> notTriggerNameList;
    private List<String> repeatJobList;
}
