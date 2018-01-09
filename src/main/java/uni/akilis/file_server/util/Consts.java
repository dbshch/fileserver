package uni.akilis.file_server.util;

/**
 * Created by leo on 12/24/17.
 */
public class Consts {
    public static final String UPLOAD_DIR = "upload-dir";
    public static final String UP_DOWN_PATH = "/updown/";
    public static final String RESUMABLE_UPLOAD_PATH = "/upload/resumable";

    /**
     * Refresh period for memory cleaning in millis.
     */
    public static final long RESUMABLE_REFRESH_PERIOD = 1 * 24 * 3600 * 1000;
}
