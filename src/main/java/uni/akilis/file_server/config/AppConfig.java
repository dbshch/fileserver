package uni.akilis.file_server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uni.akilis.file_server.filter.TokenInterceptor;
import uni.akilis.file_server.util.Consts;

/**
 * @author leo
 */
@Configuration
public class AppConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private TokenInterceptor tokenInterceptor;

    /**
     * Validating some incoming request.
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns(Consts.UP_DOWN_PATH + "**");
    }

    @Value("${CORS_TOGGLE}")
    private boolean corsToggle = false;

    /**
     * Support CORS for upload and download APIs.
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsToggle)
            registry.addMapping(Consts.UP_DOWN_PATH + "**");
    }
}
