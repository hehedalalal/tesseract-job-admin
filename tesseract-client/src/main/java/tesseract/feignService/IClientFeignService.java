package tesseract.feignService;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractExecutorResponse;

import java.net.URI;

@FeignClient("ClientFeignClient")
public interface IClientFeignService {
    @RequestLine("POST")
    TesseractExecutorResponse registry(URI uri, TesseractAdminRegistryRequest request);
}
