package uni.akilis.file_server.util;

/**
 * Created by leo on 12/24/17.
 */
public class Consts {
    public static final String UPLOAD_DIR = "upload-dir";
    public static final String UP_DOWN_PATH = "/updown/";

    /**
     * Refresh period for memory cleaning in millis.
     */
    public static final long RESUMABLE_REFRESH_PERIOD = 1 * 24 * 3600 * 1000;

    /**
     * Threshold for throttling the uploading when disk is almost full.
     */
    public static final double FISK_SPACE_THRESHOLD = 0.2;

    /**
     * Suffix of the temporary uploading file's name.
     */
    public static final String  SUFFIX = ".temp";
}
