package uni.akilis.file_server.pojo;

import uni.akilis.file_server.util.HttpUtils;

import java.io.File;
import java.util.HashSet;

/**
 * by fanxu
 * <br/>
 * Data structure of uploading file in server side.
 */
public class ResumableInfo {

    public int resumableChunkSize;
    public long resumableTotalSize;
    public String resumableIdentifier;
    public String resumableFilename;
    public String resumableRelativePath;
    /*
    In millis.
     */
    public long createdAt;

    public static class ResumableChunkNumber {
        public ResumableChunkNumber(int number) {
            this.number = number;
        }

        public int number;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ResumableChunkNumber
                    ? ((ResumableChunkNumber) obj).number == this.number : false;
        }

        @Override
        public int hashCode() {
            return number;
        }
    }

    //Chunks uploaded
    public HashSet<ResumableChunkNumber> uploadedChunks = new HashSet<ResumableChunkNumber>();

    public String resumableFilePath;

    public boolean vaild() {
        if (resumableChunkSize < 0 || resumableTotalSize < 0
                || HttpUtils.isEmpty(resumableIdentifier)
                || HttpUtils.isEmpty(resumableFilename)
                || HttpUtils.isEmpty(resumableRelativePath)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfUploadFinished() {
        //check if upload finished
        int count = (int) Math.ceil(((double) resumableTotalSize) / ((double) resumableChunkSize));
        for (int i = 1; i < count; i++) {
            if (!uploadedChunks.contains(new ResumableChunkNumber(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * rename filename with timestamp as prefix.
     *
     * @return final file.
     */
    public File renameFile(long timestamp) {
        //Upload finished, change filename.
        File file = new File(resumableFilePath);
        String parentPath = file.getAbsoluteFile().getParent();
        File newFile = new File(parentPath, timestamp + "_" + resumableFilename);
        if (file.renameTo(newFile)) {
            return newFile;
        } else
            return null;
    }
}
