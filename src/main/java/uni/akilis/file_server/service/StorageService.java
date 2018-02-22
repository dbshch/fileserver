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
        "rrGz/OeYv4agL9OGqhoRX1dQtndNshikEMyxr236oAJlaCVWkBbcCw==";
    private static String unlockCode =
        "8f3o18ONtRkJBDdKtFJS8bag2akLggDM5FPEGqX5jnut43HWteFv4h26dDgKWDwlhCiEanmXtiSloTumJvro2V44rurfcffN7nbj6SD6Hu/zf9bt0XGt+Yhu3WLUUlV1h7hY/MSqV0BhY5riKYFXDFFQAgDu+/PGcGK73jX4eDI7tXblW3dgRFrR8s+VUxW21AETVrrNyX3IbQgIwjol3xR9hGBIzmQqzMeSk7KTTEJsqAokLlasm3PHmZg8Ogoi02v9MTwWW9+9SnnxoWsPfquYYWlKOSLUd2hF16ucEvcI4D7ZOjMOntavOrOqP0WJYkU4HrcY5/bZTEuypI81CrjVD6EOJpCiedfs4tljKkdKAAGukPSqAEBBSsWlcaFXPeXH7CcBt8a01UnO5XdN2TMs+9vYn6NvKcjYCE4EpUGCbxbBPHb8flbxnfvHjWGDpsVUcI1pIPxVlhqX7MeL50PyCPMpjDoVbFOp+6JbVW4TSBosz6ZfiYECDn9WATHhzxjd99lu/LIDXD7ipiK5+qruXqZWetmfe/KdW7KEP/iufRlwTuXejYz06otyZUjgoVPKGC7Qv72e3D/FYdLS6GPbw63VVg6tlywwBhXZ19SiULS8Z7UBO39tvUlAuEnvWQvfM2p8sS/EGeIFs19bwMHfNWqgDeHNhBIA513sa8/T0k2L7n6kbATAqClRHZwR7FTdzzJwC7Icaqy91Aq1N6hFXWbHunH7LSg6dMk939DrE2NpCWf6xjf9wVowNGWthsY0yvuTlle2YdzT2Kqbt0TdpjsRn7IOkfhEb5xL7oI/th/Y25BHW/6AJc7MmpAN5MxWgln7g+FVSYVoZEqSPRjZs215A7fCIslvUJVbvVi3gUuGxBz21gL6n2aCliHxkDyj5yzVJ9ZdaS32/4v3ARDLVNiWusat7EM/ds5xRzWu6RlSayF82yl+eSF/eFVu3CxaE4R25gyj4byAKuZ6dwnxf0pII5mvR6WKhg7twjtZfUDaWd7WX9mE98i2hKdO2oJhIoFUA+vAWilRTEPwmCgeEHBfdOCs7nMLFMr7m9xb13DlinT+iXEUPll7h6cGZC6hKNp0gS2Q8Ajn/mubJ6mJf1tSm060u5hHGdU/6sMKJg3Kee/IDq0PTGKmBjp0n+cw6/7hn18XatcYhqt2eiRy1wHPY0d9K35kRNRdVG13J8lyZEh1BWxFxPAf5EuooNY9lVy9KdlUXHJkyA3SRkCtJmA2ORhBpi5811khelo5Po31yz8Sks2+PUXU8JmDDCzPSjAg5I9dg2qZOFHGr+Ti/pyBHuXSUJGNsbc+rqiyh0FNxW2IpWM7hGvz9Qa60DFrhm4vYJGAN6W4l3Snv8SDgn4iY0bJFi5vmfB8wjkPb280g8QBJiqu72fIiEU2zKmHM5GBGELSvQXaQOAfqws3BCWqHLClHfEh4OnKOI3JNSA4ykSGrEU+4wFJTb88GCs5zKgs4fw3fIz5pJPQQZq1+F1U2UzS2xLaWln9C7jDnfXeFkzlfTvZpjvdP03YdVRpL66y6HhM7nY2d0WO+vVMJVW2NO24v8wXqurUBm2Kn+DT8UT9Ve+bmjvCSO+UNiYE3kRRHbl2qRzSdGPNGqugHr7qn5bQZLSuU932R0HyOa+qOl17TdzMrtz+u6tVgERzWBnb/Bmg7eN0wSF50/Rv658XJMUTHteQP6vi+5dIIABZkOENoTl2UXmesSmuXLlqxXmskpBCVpPd87R7ksJL24QDhcsUIQ4FX6qhSVzq";

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
                    "{\"x\":%f, \"y\":%f, \"Content\":\"%s\"}", width/72*2.54,
                    height/72*2.54, encoded);
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
