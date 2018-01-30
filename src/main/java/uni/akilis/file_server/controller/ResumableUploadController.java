package uni.akilis.file_server.controller;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.akilis.file_server.config.UploadWatcher;
import uni.akilis.file_server.dao.IDao;
import uni.akilis.file_server.dto.*;
import uni.akilis.file_server.entity.UploadFile;
import uni.akilis.file_server.service.ResumableInfo;
import uni.akilis.file_server.service.ResumableInfoStorage;
import uni.akilis.file_server.service.StorageService;
import uni.akilis.file_server.util.Consts;
import uni.akilis.file_server.util.HttpUtils;
import uni.akilis.file_server.util.TimeConsume;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

/**
 * Created by leo on 12/27/17.
 * Support resumable file uploading. Show uploaded file list and provide downloading.
 */
@RestController
@RequestMapping(Consts.UP_DOWN_PATH)
public class ResumableUploadController {

    public static final String UPLOAD_DIR = Consts.UPLOAD_DIR;

    private static final Logger logger = LoggerFactory.getLogger(ResumableUploadController.class);

    /*
    Configure HTTP client.
     */
    private int timeout = 1;
    private RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(timeout * 1000)  // Connection Manager Timeout
            .setConnectTimeout(timeout * 1000)
            .setSocketTimeout(timeout * 1000).build();
    private CloseableHttpClient httpclient =
            HttpClientBuilder.create().setDefaultRequestConfig(config).build();

    // Random for file name
    private Random random = new Random();

    @Autowired
    private IDao iDao;

    @Autowired
    private StorageService storageService;

    @Autowired
    private UploadWatcher uploadWatcher;

    /**
     * List all files.
     *
     * @return
     */
    @PostMapping("getallfiles")
    public List<FileRecordDto> getListFiles() {
        List<FileRecordDto> lstFiles = this.iDao.findAllFiles();
        return lstFiles;
    }

    /**
     * Download a file with resource locator.
     *
     * @param fileId
     * @return
     */
    @PostMapping("files/{fileId:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable int fileId) {
        Resource file = storageService.loadFile(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /**
     * Check whether this uploading chunk already exists in server side.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "resumable", method = RequestMethod.GET)
    public void testChunk(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int resumableChunkNumber = getResumableChunkNumber(request);

        ResumableInfo info = null;
        try {
            info = getResumableInfo(request);
        } catch (ServletException e) {
            logger.warn("Invalid request params.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            throw e;
        }

        if (info.uploadedChunks.contains(new ResumableInfo.ResumableChunkNumber(resumableChunkNumber))) {
            response.getWriter().print("Uploaded."); //This Chunk has been Uploaded.
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    /**
     * Store this uploading chunk into file.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "resumable", method = RequestMethod.POST)
    public void uploadChunk(HttpServletRequest request, HttpServletResponse response, @RequestParam("fileInfo") String fileInfoStr) throws ServletException, IOException, InterruptedException {
        int resumableChunkNumber = getResumableChunkNumber(request);

        ResumableInfo info = null;
        try {
            info = getResumableInfo(request);
        } catch (ServletException e) {
            logger.warn("Invalid request params.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            throw e;
        }

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
            File newFile = info.renameFile();
            // check null for new file
            if (newFile == null) {
                logger.error("Rename file fail!");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("Rename file fail!");
                return;
            }
            logger.info("File stored as " + newFile.getAbsolutePath());
            UploadFile uploadFile = this.iDao.saveFile(info.createdAt, info.resumableFilename, newFile.getName(), newFile.length());
            // notify the web server which user has uploaded which file.
            FileInfo fileInfo = new Gson().fromJson(fileInfoStr, FileInfo.class);
            UploadConfirmDto uploadConfirmDto = new UploadConfirmDto(fileInfo.getToken(), fileInfo.getUserId(), fileInfo.getProjectId(), uploadFile.getId());
            if (!feedWatcher(uploadConfirmDto)) {
                Thread.sleep(1000);
                if (!feedWatcher(uploadConfirmDto)) {
                    logger.warn("Upload notification fail!\nuser id = {}, file id = {}", fileInfo.getUserId(), uploadFile.getId());
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().print("Upload notification fail!");
                    return;
                }
            }
            // Clean the uploaded file in the memory.
            ResumableInfoStorage.getInstance().remove(info);
            response.getWriter().print("One File uploaded.");
        } else {
            response.getWriter().print("Upload");
        }
    }

    /**
     * Notify the watcher that a file was uploaded just now.
     *
     * @param uploadConfirmDto
     * @return
     */
    private boolean feedWatcher(UploadConfirmDto uploadConfirmDto) {
        /*
        Build the json parameter to transfer
         */
        String jsonContent = new Gson().toJson(uploadConfirmDto);
        logger.debug("File confirming json string: " + jsonContent);
        try {
            URI uri = new URIBuilder()
                    .setScheme(uploadWatcher.getScheme())
                    .setHost(uploadWatcher.getHost())
                    .setPort(uploadWatcher.getPort())
                    .setPath(uploadWatcher.getPath())
                    .build();
            logger.debug("File confirming request URI: " + uri.toASCIIString());
            StringEntity requestEntity = new StringEntity(
                    jsonContent,
                    ContentType.APPLICATION_JSON);
            HttpPost httppost = new HttpPost(uri);
            httppost.setEntity(requestEntity);
            TimeConsume timeConsume = new TimeConsume();
            CloseableHttpResponse httpresponse = this.httpclient.execute(httppost);
            HttpEntity entity = httpresponse.getEntity();
            int code = httpresponse.getStatusLine().getStatusCode();
            if (entity != null) {
                String jsonStr = EntityUtils.toString(entity);
                if (jsonStr == null) {
                    logger.error("Upload confirm fail!\nResponse body is null!");
                    return false;
                }
                httpresponse.close();
                if (code == HttpServletResponse.SC_OK) {
                    String respToken = new Gson().fromJson(jsonStr, UploadConfirmSuccess.class).getToken();
                    if (respToken == null) {
                        logger.error("Upload confirm fail!\nExpected token = {}, response token is null! Response body = {}",
                                uploadConfirmDto.getToken(), jsonStr);
                        return false;
                    }
                    if (respToken.equals(uploadConfirmDto.getToken())) {
                        logger.info("Confirmation time cost: {} millis.", timeConsume.getTimeConsume());
                        return true;
                    } else
                        logger.error("Upload confirm fail!\nExpected token = {}, response token = {}",
                                uploadConfirmDto.getToken(), respToken);
                } else {
                    UploadConfirmFail uploadConfirmFail = new Gson().fromJson(jsonStr, UploadConfirmFail.class);
                    logger.error("Upload confirm fail!\nResponse token = {}, message = {}",
                            uploadConfirmFail.getToken(), uploadConfirmFail.getMsg());
                }
            } else {
                logger.error("Upload confirm fail!\nCode = {}, No token returned.",
                        code);
            }
        } catch (URISyntaxException e) {
            logger.error(e.toString());
        } catch (ClientProtocolException e) {
            logger.error(e.toString());
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return false;
    }

    /**
     * Get the uploading chunk number in file.
     *
     * @param request
     * @return
     */
    private int getResumableChunkNumber(HttpServletRequest request) {
        return HttpUtils.toInt(request.getParameter("resumableChunkNumber"), -1);
    }

    /**
     * Get or create a representation for this uploading file.
     *
     * @param request
     * @return
     * @throws ServletException
     */
    private ResumableInfo getResumableInfo(HttpServletRequest request) throws ServletException {
        String base_dir = UPLOAD_DIR;

        int resumableChunkSize = HttpUtils.toInt(request.getParameter("resumableChunkSize"), -1);
        long resumableTotalSize = HttpUtils.toLong(request.getParameter("resumableTotalSize"), -1);
        String resumableIdentifier = request.getParameter("resumableIdentifier");
        String resumableFilename = request.getParameter("resumableFilename");
        String resumableRelativePath = request.getParameter("resumableRelativePath");
        /*
        Here we add a ".temp" to every upload file to indicate NON-FINISHED.
        And add timestamp and random number as the prefix. The final uploaded file name will replace the timestamp
        with that ending timestamp.
         */
        long timestamp = System.currentTimeMillis();
        String resumableFilePath = new File(base_dir, timestamp + "_" + this.random.nextInt() + "_" + resumableFilename).getAbsolutePath() + Consts.SUFFIX;

        ResumableInfoStorage storage = ResumableInfoStorage.getInstance();

        ResumableInfo info = storage.get(resumableChunkSize, resumableTotalSize,
                resumableIdentifier, resumableFilename, resumableRelativePath, resumableFilePath, timestamp);
        if (!info.vaild()) {
            storage.remove(info);
            throw new ServletException("Invalid request params.");
        }
        return info;
    }
}
