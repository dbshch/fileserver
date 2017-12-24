package uni.akilis.file_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import uni.akilis.file_server.service.StorageService;

import javax.annotation.Resource;

/**
 * Created by leo on 11/19/17.
 */
@SpringBootApplication
public class Application implements CommandLineRunner{

    @Resource
    StorageService storageService;

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext appContxt = SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) throws Exception {
        storageService.init();
    }
}
