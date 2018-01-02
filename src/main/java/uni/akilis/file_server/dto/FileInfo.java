package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Created by leo on 12/24/17.
 * <br/>
 * Extra information transferred with the uploading files. Put it in the GET method as part of parameters.
 * <br/>
 * FIXME
 */
public class FileInfo implements Serializable{
    private int userId;

    public FileInfo(){}

    public FileInfo(int id) {
        this.userId = id;
    }

    // getter and setter

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
