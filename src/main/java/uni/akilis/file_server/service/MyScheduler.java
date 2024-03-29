package uni.akilis.file_server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Schedule some routines.
 * @author leo
 */
@Service
public class MyScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MyScheduler.class);

    @Autowired
    private StorageService storageService;

    /**
     * Refresh garbage in memory.
     */
    @Scheduled(cron = "${resumable.refresh.schedule}")
    void refreshResumableFiles() {
        ResumableInfoStorage storage = ResumableInfoStorage.getInstance();
        logger.info("Before cleaned: {} files in memory.", storage.filesNumber());
        logger.info("Cleaned {} files.", storage.refresh());
    }

    /**
     * Refresh garbage in disk.
     */
    @Scheduled(cron = "${zip.file.refresh.schedule}")
    void refreshZipFiles() {
        logger.info("Before cleaned: {} zip files in disk.", storageService.zipFilesNumber());
        logger.info("Actually Cleaned {} files.", storageService.refreshZipFiles());
    }
}
