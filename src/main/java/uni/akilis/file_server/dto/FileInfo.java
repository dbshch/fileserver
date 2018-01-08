package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Created by leo on 12/24/17.
 * <br/>
 * Extra information transferred with the uploading files. Put it in the GET method as part of parameters.
 * Such as authentication/authorization etc.
 * <br/>
 */
public class FileInfo implements Serializable{

    /*
    Access token for REST API
     */
    private String token;

    public FileInfo(){}

    public FileInfo(String token) {
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
