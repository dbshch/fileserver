package uni.akilis.file_server.entity;

import javax.persistence.*;

/**
 * Entity represents "upload_file" table.
 * Created by leo on 12/25/17.
 */
@Entity
@Table(name = "upload_file")
public class UploadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    /*
    Origin name.
     */
    @Column(name = "origin_name")
    private String originName;
    /*
    Unique filename.
     */
    private String filename;
    /*
    Timestamp in milliseconds.
     */
    @Column(name = "created_at")
    private long createdAt;
    /*
    File size in bytes.
     */
    private long size;

    // Need default constructor

    public UploadFile() {
    }

    public UploadFile(String originName, String filename, long createdAt, long size) {
        this.originName = originName;
        this.filename = filename;
        this.createdAt = createdAt;
        this.size = size;
    }

    //getter and setter

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "UploadFile{" +
                "id=" + id +
                ", originName='" + originName + '\'' +
                ", filename='" + filename + '\'' +
                ", createdAt=" + createdAt +
                ", size=" + size +
                '}';
    }
}
