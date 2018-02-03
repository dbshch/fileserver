package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Object containing upload-notification info will be transferred to the watcher.
 * @author leo
 */
public class UploadConfirmDto implements Serializable {
    private FileInfo fileInfo;
    private int fileId;

    // setter and getter

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public UploadConfirmDto() {
    }

    public UploadConfirmDto (FileInfo fileInfo, int fileId) {
        this.fileInfo = fileInfo;
        this.fileId = fileId;
    }


}
