package uni.akilis.file_server.config;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Configure the API path of watcher listening for upload event.
 * @author leo
 */
@Component
@ConfigurationProperties("watcher")
@Validated
public class UploadWatcher {

    private String scheme = "http";

    private String host = "localhost";

    @NotNull
    private int port = 8083;

    @NotEmpty
    private String path;

    // getter and setter

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
