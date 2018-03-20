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

    @NotNull private int port = 8080;
    private int sign_port;

    @NotEmpty
    private String path;
    private String api_key;
    private String api_secret;

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
    public void setSignPort(int sign_port) { this.sign_port = sign_port; }

    public int getSignPort() { return sign_port; }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public String getApi_key() { return api_key; }

    public String getApi_secret() { return api_secret; }

    public void setPath(String path) {
        this.path = path;
    }

    public void setApi_secret(String api_secret) {
        this.api_secret = api_secret;
    }

    public void setApi_key(String api_key) { this.api_key = api_key; }
}
