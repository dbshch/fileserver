package uni.akilis.file_server.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import uni.akilis.file_server.dao.IDao;
import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.util.Consts;


/**
 * Created by leo on 12/24/17.
 * <br/>
 * Filename here is unique with timestamp as the prefix.
 */
@Service
public class StorageService {

    @Autowired
    private IDao iDao;

    Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final Path rootLocation = Paths.get(Consts.UPLOAD_DIR);

    /**
     * Store the uploaded file.
     *
     * @param file
     * @param fileInfo
     * @return Stored file name
     */
    public String store(MultipartFile file, FileInfo fileInfo) {
        try {
            long time = System.currentTimeMillis();
            String originName = file.getOriginalFilename();
            String filename = time + "_" + originName;
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));
            this.iDao.saveFile(time, originName, filename, fileInfo);
            return filename;
        } catch (Exception e) {
            throw new RuntimeException("FAIL!");
        }
    }

    /**
     * Load a file by its id.
     *
     * @param fileId
     * @return
     */
    public Resource loadFile(int fileId) {
        try {
            String filename = this.iDao.getFilenameById(fileId);
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("FAIL!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("FAIL!");
        }
    }

    /**
     * Initialize the store directory. This method should be called after application launched immediately.
     */
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        }
    }
}
