package admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("admin.mapper")
@EnableFeignClients
public class TesseractAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TesseractAdminApplication.class, args);
    }

}
