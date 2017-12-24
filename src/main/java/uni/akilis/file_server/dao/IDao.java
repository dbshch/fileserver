package uni.akilis.file_server.dao;

import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.dto.FileRecordDto;

import java.util.List;

/**
 * Created by leo on 12/24/17.
 */
public interface IDao {
    /**
     * Archive the uploaded file with the detailed information.
     * @param time
     * @param originName
     * @param filename
     * @param fileInfo
     */
    void saveFile(long time, String originName, String filename, FileInfo fileInfo);

    /**
     * Get the archived file name by its id.
     * @param fileId
     * @return
     */
    String getFilenameById(long fileId);

    /**
     * List all files.
     * @return
     */
    List<FileRecordDto> findAllFiles();
}
