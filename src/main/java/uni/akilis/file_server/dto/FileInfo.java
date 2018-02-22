package uni.akilis.file_server.dto;

import java.io.Serializable;

/**
 * Created by leo on 12/24/17.
 * Extra information transferred with the uploading files. Put it in the GET method as part of parameters.
 * Such as authentication/authorization etc.
 */
public class FileInfo implements Serializable{

    /*
    Access token, user id, project id for REST API
     */
    private String token;
    private int userId;
    private int projectId;
    private int originFileId = -1;
    public String fileType;
    public long spid, adid;
    public String childFileType, discipline, accountName, zsh;
    public int prosid, orgid, sitemid, conid, fkId;

    public FileInfo(){}

    // getter and setter


    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getOriginFileId() {
        return originFileId;
    }

    public void setOriginFileId(int originFileId) {
        this.originFileId = originFileId;
    }
}
