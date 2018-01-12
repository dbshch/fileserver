package uni.akilis.file_server.dto;

import java.io.Serializable;

public class UploadConfirmFail implements Serializable {
    private String token;
    /*
    Confirmation fail detail
     */
    private String msg;

    public UploadConfirmFail() {}
    public UploadConfirmFail(String token, String msg) {
        this.token = token;
        this.msg = msg;
    }
    // setter and getter


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
