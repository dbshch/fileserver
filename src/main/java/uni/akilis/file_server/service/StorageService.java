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
 * Filename here is unique with timestamp and random number as the prefix.
 */
@Service
public class StorageService {

    @Autowired
    private IDao iDao;

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    private final Path rootLocation = Paths.get(Consts.UPLOAD_DIR);

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
     * Initialize the store directory. This method should be called once application launched.
     */
    public void init() {
        try {
            if(!Files.exists(rootLocation))
                Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        }
    }
}
