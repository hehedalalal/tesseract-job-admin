package tesseract.core.dto;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class TesseractAdminRegistryResDTO {
    private List<String> notTriggerNameList;
    private List<String> noExecutorList;
    private List<String> repeatJobList;
}
