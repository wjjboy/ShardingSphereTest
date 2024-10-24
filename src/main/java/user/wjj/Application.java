package user.wjj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author WuJJ
 * @description
 * @date 2024/10/24 15:55
 **/
@SpringBootApplication(scanBasePackages="user.wjj")
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        try {
            SpringApplication.run(Application.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
