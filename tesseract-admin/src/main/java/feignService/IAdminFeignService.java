package feignService;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import tesseract.core.dto.TesseractExecutorRequest;
import tesseract.core.dto.TesseractExecutorResponse;

import java.net.URI;

@FeignClient("AdminFeignClient")
public interface IAdminFeignService {
    @RequestLine("POST")
    TesseractExecutorResponse sendToExecutor(URI uri, TesseractExecutorRequest request);
}
