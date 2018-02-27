package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Object containing upload-notification success info will accept from the watcher.
 * @author leo
 */
public class UploadConfirmSuccess implements Serializable {
    private String token;
    private String msg;
    private String code;

    public UploadConfirmSuccess() {}
    public UploadConfirmSuccess(String token, String msg, String code) {
        this.token = token;
        this.msg = msg;
        this.code = code;
    }
    public UploadConfirmSuccess(String token) {
        this.token = token;
        this.msg = "null";
        this.code = "200";
    }
    
    // getter and setter


    public String getToken() {
        return token;
    }

    public String getCode() { return code; }

    public String getMsg() { return msg; }

    public void setToken(String token) {
        this.token = token;
    }
}
