package admin.config;

import admin.core.scheduler.TesseractScheduler;
import admin.core.scheduler.TesseractTriggerDispatcher;
import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feignService.IAdminFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableFeignClients
@Import(FeignClientsConfiguration.class)
public class AdminConfig {
    @Autowired
    private Decoder decoder;
    @Autowired
    private Encoder encoder;

    @Bean(destroyMethod = "destroy")
    public TesseractScheduler tesseractScheduler() {
        return new TesseractScheduler();
    }

    @Bean(destroyMethod = "destroy")
    public TesseractTriggerDispatcher tesseractTriggerDispatcher() {
        return new TesseractTriggerDispatcher();
    }

    @Bean
    public IAdminFeignService iAdminFeignService() {
        IAdminFeignService iAdminFeignService = Feign.builder().encoder(encoder).decoder(decoder)
                .target(Target.EmptyTarget.create(IAdminFeignService.class));
        return iAdminFeignService;
    }

}
