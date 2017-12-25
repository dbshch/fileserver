package uni.akilis.file_server.entity;

import javax.persistence.*;

/**
 * Created by leo on 12/25/17.
 */
@Entity
@Table(name = "upload_file")
public class UploadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "origin_name")
    private String originName;
    private String filename;
    @Column(name = "user_id")
    private int userId;
    @Column(name = "created_at")
    private long createdAt;

    // Need default constructor

    public UploadFile() {
    }

    public UploadFile(String originName, String filename, int userId, long createdAt) {
        this.originName = originName;
        this.filename = filename;
        this.userId = userId;
        this.createdAt = createdAt;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UploadFile{" +
                "id=" + id +
                ", originName='" + originName + '\'' +
                ", filename='" + filename + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
}
