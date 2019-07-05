package admin;

import admin.advice.ControllerExceptionAdvice;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@MapperScan("admin.mapper")
@EnableFeignClients
@Import(ControllerExceptionAdvice.class)
public class TesseractAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TesseractAdminApplication.class, args);
    }

}
