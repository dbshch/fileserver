package uni.akilis.file_server.dao;

import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.dto.FileRecordDto;
import uni.akilis.file_server.entity.UploadFile;

import java.util.List;

/**
 * Data read/write interfaces.
 * Created by leo on 12/24/17.
 */
public interface IDao {

    /**
     * Archive the uploaded file.
     * @param time
     * @param originName
     * @param filename
     * @param size
     * @return
     */
    UploadFile saveFile(long time, String originName, String filename, long size);

    /**
     * Get the archived file name by its id.
     * @param fileId
     * @return
     */
    String getFilenameById(int fileId);

    /**
     * List all files.
     * @return
     */
    List<FileRecordDto> findAllFiles();

    /**
     * Clear file records.
     */
    void clearFileDb();

    /**
     * Get files number.
     * @return
     */
    int countFiles();

    /**
     * Remove a record from uploading.
     * @param id
     */
    void removeUploadRecord(Integer id);
}
