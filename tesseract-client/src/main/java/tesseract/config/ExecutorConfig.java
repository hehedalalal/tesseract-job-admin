package tesseract.config;

import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tesseract.controller.ExecutorController;
import tesseract.core.executor.TesseractExecutor;
import tesseract.feignService.IClientFeignService;
import tesseract.postproccsor.TesseractBeanFactoryProcesser;

@Configuration
@Import(FeignClientsConfiguration.class)
public class ExecutorConfig {
    @Autowired
    private Decoder decoder;
    @Autowired
    private Encoder encoder;

    @Bean
    public ExecutorController executorController() {
        return new ExecutorController();
    }

    @Bean
    public TesseractExecutor tesseractExecutor() {
        return new TesseractExecutor();
    }

    @Bean
    public IClientFeignService iClientFeignService() {
        return Feign.builder().encoder(encoder).decoder(decoder)
                .target(Target.EmptyTarget.create(IClientFeignService.class));
    }

    @Bean
    public TesseractBeanFactoryProcesser tesseractBeanFactoryProcesser() {
        return new TesseractBeanFactoryProcesser();
    }
}
