package uni.akilis.file_server.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uni.akilis.file_server.dao.IDao;
import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.pojo.ResumableInfo;
import uni.akilis.file_server.service.ResumableInfoStorage;
import uni.akilis.file_server.util.Consts;
import uni.akilis.file_server.util.HttpUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by leo on 12/27/17.
 * <br/>
 * Support resumable file uploading.
 */
@Controller
@RequestMapping(Consts.RESUMABLE_UPLOAD_PATH)
public class ResumableUploadController {

    public static final String UPLOAD_DIR = Consts.UPLOAD_DIR;

    @Autowired
    private IDao iDao;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public void testChunk(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int resumableChunkNumber = getResumableChunkNumber(request);

        ResumableInfo info = getResumableInfo(request);

        if (info.uploadedChunks.contains(new ResumableInfo.ResumableChunkNumber(resumableChunkNumber))) {
            response.getWriter().print("Uploaded."); //This Chunk has been Uploaded.
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void uploadChunk(HttpServletRequest request, HttpServletResponse response, @RequestParam("fileInfo") String fileInfoStr) throws ServletException, IOException {
        FileInfo fileInfo = new Gson().fromJson(fileInfoStr, FileInfo.class);

        int resumableChunkNumber = getResumableChunkNumber(request);

        ResumableInfo info = getResumableInfo(request);

        RandomAccessFile raf = new RandomAccessFile(info.resumableFilePath, "rw");

        //Seek to position
        raf.seek((resumableChunkNumber - 1) * (long) info.resumableChunkSize);

        //Save to file
        InputStream is = request.getInputStream();
        long readed = 0;
        long content_length = request.getContentLength();
        byte[] bytes = new byte[1024 * 100];
        while (readed < content_length) {
            int r = is.read(bytes);
            if (r < 0) {
                break;
            }
            raf.write(bytes, 0, r);
            readed += r;
        }
        raf.close();


        //Mark as uploaded.
        info.uploadedChunks.add(new ResumableInfo.ResumableChunkNumber(resumableChunkNumber));
        if (info.checkIfUploadFinished()) { //Check if all chunks uploaded, and change filename
            long timestamp = System.currentTimeMillis();
            File newFile = info.renameFile(timestamp);
            ResumableInfoStorage.getInstance().remove(info);
            System.out.println("File stored as " + newFile.getAbsolutePath());
            this.iDao.saveFile(timestamp, info.resumableFilename, newFile.getName(), fileInfo);
            response.getWriter().print("All finished.");
        } else {
            response.getWriter().print("Upload");
        }
    }

    private int getResumableChunkNumber(HttpServletRequest request) {
        return HttpUtils.toInt(request.getParameter("resumableChunkNumber"), -1);
    }

    private ResumableInfo getResumableInfo(HttpServletRequest request) throws ServletException {
        String base_dir = UPLOAD_DIR;

        int resumableChunkSize = HttpUtils.toInt(request.getParameter("resumableChunkSize"), -1);
        long resumableTotalSize = HttpUtils.toLong(request.getParameter("resumableTotalSize"), -1);
        String resumableIdentifier = request.getParameter("resumableIdentifier");
        String resumableFilename = request.getParameter("resumableFilename");
        String resumableRelativePath = request.getParameter("resumableRelativePath");
        //Here we add a ".temp" to every upload file to indicate NON-FINISHED
        String resumableFilePath = new File(base_dir, resumableFilename).getAbsolutePath() + ".temp";

        ResumableInfoStorage storage = ResumableInfoStorage.getInstance();

        ResumableInfo info = storage.get(resumableChunkSize, resumableTotalSize,
                resumableIdentifier, resumableFilename, resumableRelativePath, resumableFilePath);
        if (!info.vaild()) {
            storage.remove(info);
            throw new ServletException("Invalid request params.");
        }
        return info;
    }
}
