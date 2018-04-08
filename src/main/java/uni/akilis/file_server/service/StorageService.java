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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.Hibernate;

import java.io.File;
import java.io.IOException;

import com.foxit.gsdk.PDFException;
import com.foxit.gsdk.PDFLibrary;
import com.foxit.gsdk.PDFException;
import com.foxit.gsdk.image.Image;
import com.foxit.gsdk.pdf.PDFDocument;
import com.foxit.gsdk.pdf.PDFPage;
import com.foxit.gsdk.pdf.Progress;
import com.foxit.gsdk.pdf.signature.Signature;
import com.foxit.gsdk.pdf.signature.Signature.KeyStoreInfo;
import com.foxit.gsdk.utils.FileHandler;
import com.foxit.gsdk.utils.RectF;
import com.foxit.gsdk.utils.SizeF;

import static uni.akilis.file_server.util.CreateZipFileFromMultipleFilesWithZipOutputStream.createZip;


/**
 * Created by leo on 12/24/17.
 * Filename here is unique with timestamp and random number as the prefix.
 * Zip file's name here is unique with timestamp as the suffix.
 */
@Service
public class StorageService {

    @Autowired private IDao iDao;

    private static String license_id =
        "YnBB1tW+1jrEnw8meEhS4Zzztq6tRQIasV4sjy0FtuxMhv+goJuncA==";
    private static String unlockCode =
        "8f3o1sFttW8NAgeay0Bit8UszjFPuIB0LZPcwbnIzErJv41Ade19GpdwTu/5ptrwl3nV+bjcygs2j6DYt3Jnb+3guO0ggpwcCc54bJUD04Ly5+TcAw0O+od2VXdf0AqnGx2Jzaa9yibB4DrA4LJXEJfEuLgsRbod8ZARh/cJANIg6lgugDa0WEhWqHDVEZfZtvMX1SiBpxH4fagIwjqls8z3wn0wIcQWSXGZethCj0sLg997lCGwznbWPH76uEHpDldVH+93/i/sXGLhgWUSQcZ5YpNl3F3B0TBqCG77Bb7kzbD27JhNFAgTh+Ly4zspHIgKnKTzXygON2PGprkTy0JfBBNTu8+gyE6sjtEGT4vIvsFTPV1TPRZ9/9bdUr1B/bt+stMuuUiJGCJq7iNzvML9MIthhX+dSo7eYsI+ufaDvDJSVHrDyIEcXTSoE+2pzCvIYti1k6hfKHLNAx47IkjWaX6YChNyRDoh0U7jJ2Z8EtApPxeg4JK5JL9ZFwIesAOi+RV6wlDnWOwT6rCJ0Rss7iAVwJPbuqSLU/X97+7X8+ztFB2A3m3jJVGNY7NAO39V8CIJqA4UohQ5/m42Q40jv8UKrV5oR21B8TLi0EouGUoB+ZJzAmIXxs+V3nLp2pMpwo1MJ/ga4OHQkD/d/KIYVB4UtQJ15nT/dFxzoy/+PcV38RgGrJhv5rP7zzXYyBti5BXGhmBiJxInrNmAJy3KeBKliKexiRKlMp1iZFq86SJ0v+Sct2pbZ1l7z006r9BelwkLdgCmHOfZbM3GsEKBe4c5twttF+Tqy9NI6obkkikrAUKgbdj5pMjCjmE62PtIl50iRJRN9jvrRCwqrxKws0GJLs1n1b/H+o1oB8Q8TOPAEBf4MqhHjUGKDtPlzAlD3F75SeegHEGrSpBgOzguMJK/Tpz9ugMYysvxMz+RP3cAWoCrXPK+13CZlv7VAFQq6V+mbjPGyY3fmFey4ARn+GgwRUxjAwQOeFDHdOqgVWdkGZH/ujLlwCdXI+rOaRD+Fq/RzBFYOb4DmVijGBEniLDoUCjYE4bgvxZqzPioFVE6NdJm1ATcBvkRPMdZ+2UBfy0oAxpKnRxQFnbRKW9GSqNyu4f7uIypZTPceoYHAW93RrxEqI9Aowo+x1ODkvtrK0HnnFIG+Q1DGS/993PynopI/sw435WwEEwCxxAKvtTUlbPyNAjmo+qPGV3OTDr8uDebGeY7CxWVbwmn7Tz/TkdqgtuLgO3ucwpRkEbTZB6t2up8M6uNLZ55wVgAg1tfje+eC2U+tqQGWF2d1JFOoLRv+7lgQ73O9xPbdBeNjQeC0RX65231yJEnxbwWQZmC9XFgjNLi1XPUroZgIzr/NlWC+BeELw+xuahVdVx7zr/uyCwNL80dGOt3eTJM3yI/gXjjYIOvfw==";

    private static FileHandler handler = null;

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    private final Path rootLocation = Paths.get(Consts.UPLOAD_DIR);
    private final Path zipFileLocation = Paths.get(Consts.ZIP_FILE_DIR);
    static {
        try {
            System.load("/usr/local/lib/libfsdk_java_linux64.so");
        } catch (UnsatisfiedLinkError e) {
            logger.error("Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

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
                logger.info("Getting file {}", filename);
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

    public String loadBase64(int fileId) {
        try {
            String filename = this.iDao.getFilenameById(fileId);
            Path file = rootLocation.resolve(filename);
            File filefile = file.toFile();
            FileInputStream inputFile = new FileInputStream(filefile);
            byte[] buffer = new byte[(int) filefile.length()];
            try {
                inputFile.read(buffer);
                inputFile.close();
                logger.info("Getting file {}'s base64'", filename);
                return(new String(Base64.encodeBase64(buffer)));
            } catch (IOException e) {
                logger.error(e.toString());
                return null;
            }
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
            return null;
        }
    }

    public String loadBase64Info(int fileId) {
        try {
            String filename = this.iDao.getFilenameById(fileId);
            Path filepath = rootLocation.resolve(filename);
            int memorySize = 10 * 1024 * 1024;
            boolean scaleable = true;
            float width = 0;
            float height = 0;
            String inputSrcFile = filepath.toString();
            File file = new File(inputSrcFile);
            if (!(file.exists() && file.isFile())) {
                logger.error("Failed: The input file is not exist!");
                return null;
            }
            PDFLibrary pdfLibrary = PDFLibrary.getInstance();
            try {
                pdfLibrary.initialize(memorySize, scaleable);
                pdfLibrary.unlock(license_id, unlockCode);
                int type = pdfLibrary.getLicenseType();
                if (type == PDFLibrary.LICENSETYPE_EXPIRED ||
                    type == PDFLibrary.LICENSETYPE_INVALID) {
                    logger.error("License is invalid or expired!!!");
                    return null;
                }
            } catch (PDFException e) {
                e.printStackTrace();
                logger.error("Failed to initlize and unlock the library");
                return null; // exit
            }
            try {
                PDFDocument doc = null;
                try {
                    handler = FileHandler.create(inputSrcFile,
                                                 FileHandler.FILEMODE_READONLY);
                    doc = PDFDocument.open(handler, null);
                } catch (PDFException e) {
                    e.printStackTrace();
                    logger.error("Failed to open PDF Doument.");
                    return null; // exit
                }
                PDFPage page = doc.getPage(0);
                SizeF size = page.getSize();
                width = size.getWidth();
                height = size.getHeight();
                // logger.info(String.valueOf(doc.countSignatures()));
                doc.close();
            } catch (PDFException e) {
                return null;
            }
            if (handler != null) {
                try {
                    handler.release();
                } catch (PDFException e) {
                    e.printStackTrace();
                    logger.error("Failed to release file handle.");
                } finally {
                    pdfLibrary.destroy();
                }
            } else {
                pdfLibrary.destroy();
            }
            File filefile = filepath.toFile();
            FileInputStream inputFile = new FileInputStream(filefile);
            byte[] buffer = new byte[(int)filefile.length()];
            try {
                inputFile.read(buffer);
                inputFile.close();
                String encoded = new String(Base64.encodeBase64(buffer));
                String returnData = String.format(
                    "{\"x\":%f, \"y\":%f, \"pdf\":\"%s\"}", width/72*25.4,
                    height/72*25.4, encoded);
                return returnData;
            } catch (IOException e) {
                logger.error(e.toString());
                return null;
            }
        } catch (FileNotFoundException e) {
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
            else {
                File zipDir = new File(Consts.ZIP_FILE_DIR);
                for(File file: zipDir.listFiles()){
                    file.delete();
                }
            }
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

    /**
     * Map a key for batch files.
     * @param filesId
     * @return
     */
    private String convertToZipFileUrl(int[] filesId) {
        StringBuilder sb = new StringBuilder();
        Arrays.sort(filesId);
        for (int id : filesId) {
            sb.append(id)
                    .append("_");
        }
        return sb.toString();
    }

    /**
     * Load a zip file by its URL.
     * @param url
     * @return
     */
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

    /**
     * Get number of the retained zip files.
     * @return
     */
    public int zipFilesNumber() {
        return zipFiles.size();
    }


    /**
     * Clean the outdated zip files.
     * @return
     */
    public int refreshZipFiles() {
        int cnt = 0;
        synchronized (zipFiles) {
            zipFiles.clear();
            File zipDir = new File(Consts.ZIP_FILE_DIR);
            for (File file: zipDir.listFiles()) {
                if (file.delete())
                    cnt++;
            }
        }
        return cnt;
    }
}
