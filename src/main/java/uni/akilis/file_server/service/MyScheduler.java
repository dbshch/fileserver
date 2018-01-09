package uni.akilis.file_server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MyScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MyScheduler.class);

    @Scheduled(cron = "${resumable.refresh.schedule}")
    void refreshResumableFiles() {
        ResumableInfoStorage storage = ResumableInfoStorage.getInstance();
        logger.info("Before cleaned: {} files in memory.", storage.filesNumber());
        logger.info("Cleaned {} files.", storage.refresh());
    }
}
