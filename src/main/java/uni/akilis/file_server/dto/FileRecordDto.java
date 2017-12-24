package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Created by leo on 12/24/17.
 */
public class FileRecordDto implements Serializable {
    private int fileId;
    private String filename;
    private int userId;
    private String date;

    // getter and setter

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
