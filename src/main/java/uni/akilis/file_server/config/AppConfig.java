package uni.akilis.file_server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uni.akilis.file_server.filter.TokenInterceptor;
import uni.akilis.file_server.util.Consts;

/**
 * Validating the incoming request.
 * @author leo
 */
@Configuration
public class AppConfig extends WebMvcConfigurerAdapter{
    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns(Consts.UP_DOWN_PATH + "**");
    }
}
