package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Object containing signed file's info from CA
 * @author dbshch
 */
public class signRet implements Serializable {
    private int ret_code;
    private String ret_msg;
    private String sign_url;

    // setter and getter

    public String getSign_url() { return sign_url; }

    public String getRet_msg() { return ret_msg; }

    public int getRet_code() { return ret_code; }

    public signRet() {}

    public signRet(int ret_code, String sign_url, String ret_msg) {
        this.ret_code = ret_code;
        this.sign_url = sign_url;
        this.ret_msg = ret_msg;
    }
}
