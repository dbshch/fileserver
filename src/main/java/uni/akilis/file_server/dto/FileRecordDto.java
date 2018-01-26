package uni.akilis.file_server.dto;

import uni.akilis.file_server.entity.UploadFile;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leo on 12/24/17.
 * List of uploaded files which would be transferred to client side.
 */
public class FileRecordDto implements Serializable {
    private int fileId;
    private String filename;
    private String date;
    private long bytes;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FileRecordDto(UploadFile uploadFile) {
        this.fileId = uploadFile.getId();
        this.filename = uploadFile.getOriginName();
        this.date = dateFormat.format(new Date(uploadFile.getCreatedAt()));
        this.bytes = uploadFile.getSize();
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
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
