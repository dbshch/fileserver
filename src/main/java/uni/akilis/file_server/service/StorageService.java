package uni.akilis.file_server.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
 * Zip file's name here is unique with timestamp as the suffix.
 */
@Service
public class StorageService {

    @Autowired
    private IDao iDao;

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    private final Path rootLocation = Paths.get(Consts.UPLOAD_DIR);
    private final Path zipFileLocation = Paths.get(Consts.ZIP_FILE_DIR);

    /*
    Manage the newly created zip files. The key is made up of files ID,
    value is the zip file's name.
     */
    private Map<String, String> zipFiles = new HashMap<>();

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
                return null;
            }
        } catch (MalformedURLException e) {
            logger.error(e.toString());
            return null;
        }
    }

    /**
     * Initialize the store directory. This method should be called once application launched.
     */
    public void init() {
        try {
            if (!Files.exists(rootLocation))
                Files.createDirectory(rootLocation);
            if (!Files.exists(zipFileLocation))
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
    public String compressFiles(int[] filesId, String zipFilename) {
        if (filesId == null || filesId.length == 0 || zipFilename == null || zipFilename.isEmpty()) {
            logger.error("Wrong parameters!");
            return null;
        }
        String url = convertToZipFileUrl(filesId);
        if (this.zipFiles.containsKey(url)) {
            return url;
        }
        List<String> srcFiles = new ArrayList<>();
        for (int id : filesId) {
            String filename = this.iDao.getFilenameById(id);
            if (!filename.isEmpty())
                srcFiles.add(new File(Consts.UPLOAD_DIR, filename).getAbsolutePath());
            else
                logger.warn("Missing file with ID {}", id);
        }
        Path file = zipFileLocation.resolve(zipFilename);
        String absFilePath = file.toAbsolutePath().toString();
        if (!createZip(absFilePath, srcFiles)) {
            logger.error("Compression fail: {}", absFilePath);
            return null;
        }
        logger.info("Stored a zip file: " + absFilePath);
        this.zipFiles.put(url, zipFilename);
        return url;
    }

    private String convertToZipFileUrl(int[] filesId) {
        StringBuilder sb = new StringBuilder();
        Arrays.sort(filesId);
        for (int id : filesId) {
            sb.append(id)
                    .append(",");
        }
        return sb.toString();
    }


    public Resource loadFile(String url) {
        if (!this.zipFiles.containsKey(url)) {
            logger.error("Zip file not found! url = {}", url);
            return null;
        }
        try {
            String filename = this.zipFiles.get(url);
            Path file = zipFileLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                logger.error("File not exists: {}", filename);
                return null;
            }
        } catch (MalformedURLException e) {
            logger.error(e.toString());
            return null;
        }
    }
}
