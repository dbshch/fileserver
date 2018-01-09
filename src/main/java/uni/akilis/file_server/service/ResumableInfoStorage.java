package uni.akilis.file_server.service;

import org.springframework.scheduling.annotation.Scheduled;
import uni.akilis.file_server.pojo.ResumableInfo;
import uni.akilis.file_server.util.Consts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * by fanxu
 * <br/>
 * Manage the uploading files in server side.
 */
public class ResumableInfoStorage {

    //Single instance
    private ResumableInfoStorage() {
    }
    private static ResumableInfoStorage sInstance;

    public static synchronized ResumableInfoStorage getInstance() {
        if (sInstance == null) {
            sInstance = new ResumableInfoStorage();
        }
        return sInstance;
    }

    //resumableIdentifier --  ResumableInfo
    private HashMap<String, ResumableInfo> mMap = new HashMap<String, ResumableInfo>();

    /**
     * Get ResumableInfo from mMap or Create a new one.
     * @param resumableChunkSize
     * @param resumableTotalSize
     * @param resumableIdentifier
     * @param resumableFilename
     * @param resumableRelativePath
     * @param resumableFilePath
     * @return
     */
    public synchronized ResumableInfo get(int resumableChunkSize, long resumableTotalSize,
                             String resumableIdentifier, String resumableFilename,
                             String resumableRelativePath, String resumableFilePath) {

        ResumableInfo info = mMap.get(resumableIdentifier);

        if (info == null) {
            info = new ResumableInfo();

            info.resumableChunkSize     = resumableChunkSize;
            info.resumableTotalSize     = resumableTotalSize;
            info.resumableIdentifier    = resumableIdentifier;
            info.resumableFilename      = resumableFilename;
            info.resumableRelativePath  = resumableRelativePath;
            info.resumableFilePath      = resumableFilePath;
            info.createdAt = System.currentTimeMillis();

            mMap.put(resumableIdentifier, info);
        }
        return info;
    }

    /**
     * ResumableInfo
     * @param info
     */
    public void remove(ResumableInfo info) {
       mMap.remove(info.resumableIdentifier);
    }

    /**
     * Clean the outdated files in the memory.
     * @return Cleaned number.
     */
    int refresh() {
        int cnt = 0;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, ResumableInfo>> it = mMap.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue().createdAt > Consts.RESUMABLE_REFRESH_PERIOD) {
                it.remove();
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Files number in memory.
     * @return
     */
    public int filesNumber() {
        return mMap.size();
    }
}
