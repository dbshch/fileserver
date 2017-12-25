package uni.akilis.file_server.dto;

import uni.akilis.file_server.entity.UploadFile;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leo on 12/24/17.
 */
public class FileRecordDto implements Serializable {
    private int fileId;
    private String filename;
    private int userId;
    private String date;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FileRecordDto(UploadFile uploadFile) {
        this.fileId = uploadFile.getId();
        this.filename = uploadFile.getOriginName();
        this.userId = uploadFile.getUserId();
        this.date = dateFormat.format(new Date(uploadFile.getCreatedAt()));
    }

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

    public static List<FileRecordDto> transform(Iterable<UploadFile> files) {
        List<FileRecordDto> fileRecordDtoList = new ArrayList<>();
        if (files != null) {
            for (UploadFile uploadFile: files) {
                FileRecordDto fileRecordDto = new FileRecordDto(uploadFile);
                fileRecordDtoList.add(fileRecordDto);
            }
        }
        return fileRecordDtoList;
    }
}
