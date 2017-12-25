package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Created by leo on 12/24/17.
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
