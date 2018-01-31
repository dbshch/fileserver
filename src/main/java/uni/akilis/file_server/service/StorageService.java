package uni.akilis.file_server.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import uni.akilis.file_server.dao.IDao;
import uni.akilis.file_server.util.Consts;

import static uni.akilis.file_server.util.CreateZipFileFromMultipleFilesWithZipOutputStream.createZip;


/**
 * Created by leo on 12/24/17.
 * Filename here is unique with timestamp and random number as the prefix.
 */
@Service
public class StorageService {

    @Autowired
    private IDao iDao;

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    private final Path rootLocation = Paths.get(Consts.UPLOAD_DIR);
    private final Path zipFileLocation = Paths.get(Consts.ZIP_FILE_DIR);

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
            if (resource.exists()) {
                return resource;
            } else {
                logger.error("File not exists: {}", filename);
                throw new RuntimeException("FAIL!");
            }
        } catch (MalformedURLException e) {
            logger.error(e.toString());
            throw new RuntimeException("FAIL!");
        }
    }

    /**
     * Initialize the store directory. This method should be called once application launched.
     */
    public void init() {
        try {
            if (!Files.exists(rootLocation))
                Files.createDirectory(rootLocation);
            if (Files.exists(zipFileLocation))
                Files.createDirectory(zipFileLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        }
    }

    /**
     * Compress files into a zip file.
     *
     * @param filesId
     * @param zipFilename filename of the compressed file
     * @return Resource which is in representation of the compressed file.
     */
    public Resource compressFiles(int[] filesId, String zipFilename) {
        if (filesId == null || filesId.length == 0 || zipFilename == null || zipFilename.isEmpty()) {
            logger.error("Wrong parameters!");
            throw new RuntimeException("FAIL!");
        }
        List<String> srcFiles = new ArrayList<>();
        for (int id : filesId) {
            String filename = this.iDao.getFilenameById(id);
            if (!filename.isEmpty())
                srcFiles.add(filename);
            else
                logger.warn("Missing file with ID {}", id);
        }
        Path file = zipFileLocation.resolve(zipFilename);
        String absFilePath = file.toAbsolutePath().toString();
        if (!createZip(absFilePath, srcFiles)) {
            logger.error("Compression fail: {}", absFilePath);
            throw new RuntimeException("FAIL!");
        }
        Resource resource = null;
        try {
            resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                logger.error("File not exists: {}", zipFilename);
                throw new RuntimeException("FAIL!");
            }
        } catch (MalformedURLException e) {
            logger.error(e.toString());
        }
        return resource;
    }
}
