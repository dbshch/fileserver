package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Object containing upload-notification info will be transferred to the watcher.
 * @author leo
 */
public class UploadConfirmDto implements Serializable {
    private String token;
    private int userId;
    private int projectId;
    private int fileId;

    // setter and getter

    public UploadConfirmDto() {
    }

    public UploadConfirmDto(String token, int userId, int projectId, int fileId) {
        this.token = token;
        this.userId = userId;
        this.projectId = projectId;
        this.fileId = fileId;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }
}
