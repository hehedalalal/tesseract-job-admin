package admin.config;

import admin.core.scheduler.TesseractScheduler;
import admin.core.scheduler.TesseractTriggerDispatcher;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
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

    /**
     * 配置调度器
     *
     * @return
     */
    @Bean
    public TesseractScheduler tesseractScheduler() {
        return new TesseractScheduler();
    }

    /**
     * 配置调度分派器
     *
     * @return
     */
    @Bean
    public TesseractTriggerDispatcher tesseractTriggerDispatcher() {
        return new TesseractTriggerDispatcher();
    }

    /**
     * 配置feign服务
     *
     * @return
     */
    @Bean
    public IAdminFeignService iAdminFeignService() {
        IAdminFeignService iAdminFeignService = Feign.builder().encoder(encoder).decoder(decoder)
                .target(Target.EmptyTarget.create(IAdminFeignService.class));
        return iAdminFeignService;
    }

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
