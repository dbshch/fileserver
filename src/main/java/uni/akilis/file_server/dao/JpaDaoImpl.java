package uni.akilis.file_server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.dto.FileRecordDto;
import uni.akilis.file_server.entity.UploadFile;
import uni.akilis.file_server.repository.UploadFileRepo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leo on 12/25/17.
 */
@Component
public class JpaDaoImpl implements IDao{

    @Autowired
    private UploadFileRepo uploadFileRepo;

    @Override
    public UploadFile saveFile(long time, String originName, String filename, FileInfo fileInfo) {
        UploadFile uploadFile = new UploadFile(originName, filename, fileInfo.getUserId(), time);
        return this.uploadFileRepo.save(uploadFile);
    }

    @Override
    public String getFilenameById(int fileId) {
        return this.uploadFileRepo.findFilenameById(fileId);
    }

    @Override
    public List<FileRecordDto> findAllFiles() {
        return FileRecordDto.transform(this.uploadFileRepo.findAll());
    }

    @Override
    public void clearFileDb() {
        this.uploadFileRepo.deleteAll();
    }

    @Override
    public int countFiles() {
        return (int) this.uploadFileRepo.count();
    }
}
