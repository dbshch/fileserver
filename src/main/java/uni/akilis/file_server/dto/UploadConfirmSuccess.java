package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Object containing upload-notification success info will accept from the watcher.
 * @author leo
 */
public class UploadConfirmSuccess implements Serializable {
    private String token;

    public UploadConfirmSuccess() {}
    public UploadConfirmSuccess(String token) {
        this.token = token;
    }
    // getter and setter


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
