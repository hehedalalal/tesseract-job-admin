package admin.config;

import admin.core.scheduler.TesseractScheduleBoot;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import feign.Feign;
import feign.Request;
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
     * 启动器
     *
     * @return
     */
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public TesseractScheduleBoot tesseractScheduler() {
        return new TesseractScheduleBoot();
    }


    /**
     * 配置feign服务
     *
     * @return
     */
    @Bean
    public IAdminFeignService iAdminFeignService() {
        Request.Options options = new Request.Options(3 * 1000, 3 * 1000, true);
        IAdminFeignService iAdminFeignService = Feign.builder().encoder(encoder).decoder(decoder).options(options)
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
