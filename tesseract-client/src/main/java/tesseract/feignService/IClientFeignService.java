package tesseract.feignService;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import tesseract.core.dto.TesseractAdminJobNotify;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractExecutorResponse;
import tesseract.core.dto.TesseractHeartbeatRequest;

import java.net.URI;

@FeignClient("ClientFeignClient")
public interface IClientFeignService {
    @RequestLine("POST")
    TesseractExecutorResponse registry(URI uri, TesseractAdminRegistryRequest request);

    @RequestLine("POST")
    TesseractExecutorResponse notify(URI uri, TesseractAdminJobNotify tesseractAdminJobNotify);

    @RequestLine("POST")
    TesseractExecutorResponse heartbeat(URI uri, TesseractHeartbeatRequest heartBeatRequest);
}
