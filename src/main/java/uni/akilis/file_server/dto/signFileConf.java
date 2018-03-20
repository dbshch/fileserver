package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Object containing lists of files needed to be signed
 * @author dbshch
 */
public class signFileConf implements Serializable {
    private String id, fileType, fileName;
    private float x, y;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFileType() { return fileType; }

    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileName() { return fileName; }

    public void setx(float x) { this.x = x; }
    public float getx() { return x; }

    public void sety(float y) { this.y = y; }
    public float gety() { return y; }
}
