package uni.akilis.file_server.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

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
    public ResponseEntity<Resource> getFile(@PathVariable int fileId, HttpServletResponse response) {
        Resource file = storageService.loadFile(fileId);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new RuntimeException();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(file);
    }

    @PostMapping("filesbase64/{fileId:.+}")
    public ResponseEntity<String> getbase64File(@PathVariable int fileId, HttpServletResponse response) {
        Resource file = storageService.loadFile(fileId);        
        String fileencoded = storageService.loadBase64(fileId);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new RuntimeException();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream").body(fileencoded);
    }

    @PostMapping("base64")
    public ResponseEntity<String> base64(@RequestParam("fileid") int fileid,
                                         @RequestParam("sqcode") String sqcode,
                                         HttpServletResponse response) {
        Resource file = storageService.loadFile(fileid);
        String fileencoded = storageService.loadBase64(fileid);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new RuntimeException();
        }
        String transData =
            "{\"Result\": \"true\",\"ErrorCode\":\"100\",\"Message\": \"\",\"Data\":{\"Content\":\"" + fileencoded + "\"}}";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"")
            .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
            .body(transData);
    }

    @PostMapping("filesbase64info/{fileId:.+}")
    public ResponseEntity<String>
    getbase64withInfo(@PathVariable int fileId, HttpServletResponse response) {
        Resource file = storageService.loadFile(fileId);
        String fileencoded = storageService.loadBase64Info(fileId);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new RuntimeException();
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"")
            .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
            .body(fileencoded);
    }
    /**
     * Compress files into a zip file and return the URL for downloading this zip file next.
     * @param filesId
     * @param filename
     * @param response
     * @throws IOException
     */
    @PostMapping(value = "getfiles")
    public void downloadBatchFiles(@RequestParam("filesId") int[] filesId,
                                   @RequestParam("filename") String filename,
                                   HttpServletResponse response) throws IOException {
        String zipFilename = filename + "_" + System.currentTimeMillis() + ".zip";
        String url = storageService.compressFiles(filesId, zipFilename);
        if (url == null)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        else if (url.isEmpty())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else
            response.getWriter().print(url);
    }

    /**
     * Download a zip file.
     * @param url
     * @param response
     * @return
     */
    @GetMapping(value = "getzipfile/{url:.+}")
    public ResponseEntity<Resource> downloadZipFile(@PathVariable String url, HttpServletResponse response) {
        Resource zipFile = storageService.loadFile(url);
        if (zipFile == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new RuntimeException();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFile.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(zipFile);
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
            // In order to avoid memory garbage like an "island" and cause the user cannot upload the same file anymore,
            // we should clean the uploaded file in the memory first.
            ResumableInfoStorage.getInstance().remove(info);

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
            UploadConfirmDto uploadConfirmDto = new UploadConfirmDto(fileInfo, uploadFile.getId());
            String retMsg;
            logger.info(String.valueOf(fileInfo.getUserId()));
            retMsg = feedWatcher(uploadConfirmDto);
            if (retMsg != "succ") {
                logger.warn(
                    "Upload notification fail!\nuser id = {}, file id = {}",
                    fileInfo.getUserId(), uploadFile.getId());
                response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print(retMsg);
                return;
            }
            response.getWriter().print(String.valueOf(uploadFile.getId()));
        } else {
            response.getWriter().print("Upload");
        }
    }

    /**
     * Sign one file.
     *
     */
    @PostMapping("sign")
    public ResponseEntity<String>
    sign(@RequestParam("document_no") String doc_no,
         @RequestParam("fileid") int fileid, @RequestParam("x") float x,
         @RequestParam("y") float y, @RequestParam("isManual") int isManual,
         @RequestParam("redirect_url") String redirect_url,
         HttpServletRequest request, HttpServletResponse response) {
        Resource file = storageService.loadFile(fileid);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new RuntimeException();
        }
        try {
            byte[] bytes = doc_no.getBytes("UTF-8");
            doc_no = Base64.getEncoder().encodeToString(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        logger.debug(doc_no);
        String loadfile = storageService.loadBase64Info(fileid);
        loadFileInfo loadinfo = new Gson().fromJson(loadfile, loadFileInfo.class);
        float signx, signy;
        signx = (loadinfo.getwidth() - x - 80) / loadinfo.getwidth() * 50000;
        signy = y / loadinfo.getheight() * 50000;
        String position = String.format(
            ",\"position\":{\"page\":\"1\",\"x\":\"%f\",\"y\":\"%f\"}", signx,
            signy);
        String data;
        String host = request.getServerName();
        int portNumber = request.getServerPort();
        host = host + ":" + String.valueOf(portNumber);
        logger.debug(host);
        if (isManual == 0) {
            logger.debug("x: {}, y: {}", signx, signy);
            data = String.format(
                "{\"api_key\" : \"%s\",\"api_secret\" : \"%s\",\"seal\": {\"document_no\" : \"%s\",\"pdf\":\"%s\",\"return_url\":\"%s\",\"redirect_url\":\"%s\",\"show_page\":%s,\"type\":\"%s\"%s}}",
                uploadWatcher.getApi_key(), uploadWatcher.getApi_secret(), doc_no, loadinfo.getPdf(),
                "http://" + host + "/updown/signed",
                redirect_url, "false", "position", position);
        }
        else{
            data = String.format(
                "{\"api_key\" : \"%s\",\"api_secret\" : \"%s\",\"seal\": {\"document_no\" : \"%s\",\"pdf\":\"%s\",\"return_url\":\"%s\",\"redirect_url\":\"%s\",\"show_page\":%s,\"type\":\"%s\"%s}}",
                uploadWatcher.getApi_key(), uploadWatcher.getApi_secret(),
                doc_no, loadinfo.getPdf(),
                "http://" + host + "/updown/signed",
                redirect_url, "true", "auto", "");
        }
        try {
            URI uri = new URIBuilder()
                          .setScheme("http")
                          .setHost(uploadWatcher.getHost())
                          .setPort(uploadWatcher.getSignPort())
                          .setPath("/seal/userSignExt")
                          .build();
            StringEntity requestEntity =
                new StringEntity(data, ContentType.APPLICATION_JSON);
            HttpPost httppost = new HttpPost(uri);
            httppost.setEntity(requestEntity);
            TimeConsume timeConsume = new TimeConsume();
            CloseableHttpResponse httpresponse =
                this.httpclient.execute(httppost);
            HttpEntity entity = httpresponse.getEntity();
            int code = httpresponse.getStatusLine().getStatusCode();
            if (entity != null) {
                String jsonStr = EntityUtils.toString(entity);
                logger.debug(jsonStr);
                httpresponse.close();
                if (code == HttpServletResponse.SC_OK) {
                    signRet resp =
                        new Gson()
                            .fromJson(jsonStr, signRet.class);
                    return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE,
                                "application/octet-stream")
                        .body(resp.getSign_url());
                }
            } else {
                logger.error(
                    "Sign error {}, no result returned.",
                    code);
            }
        } catch (URISyntaxException e) {
            logger.error(e.toString());
        } catch (ClientProtocolException e) {
            logger.error(e.toString());
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
            .body("fail");
    }

    /**
     * Redirect the page to the specified one.
     *
     */
    @PostMapping("redirectSign/{urlBase64:.+}")
    public ResponseEntity<String>
    redirectSign(@PathVariable String urlBase64,
                 @RequestBody String jsonStr,
                 HttpServletResponse response) {
                     logger.debug(jsonStr);
        byte[] decoded = null;
        try {
            byte[] bytes = urlBase64.getBytes("UTF-8");
            decoded = Base64.getDecoder().decode(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = new String(decoded);
        return ResponseEntity.ok()
            .body("<html><script language=\"javascript\">window.location.replace(\"" + url + "\")</script></html>");
    }

    /**
     * Sign a series of files.
     *
     */
    @PostMapping("batchSign")
    public ResponseEntity<String>
    sign(@RequestParam("fileConf") String fileConf,
         @RequestParam("redirect_url") String redirect_url,
         HttpServletRequest request, HttpServletResponse response) {
        String host = request.getServerName();
        int portNumber = request.getServerPort();
        host = host + ":" + String.valueOf(portNumber);
        List<signFileConf> filesConf = new Gson().fromJson(
            fileConf, new TypeToken<List<signFileConf>>() {}.getType());
        String fileData = "[";
        int flg = 0;
        for (signFileConf conf : filesConf){
            Resource file = storageService.loadFile(Integer.parseInt(conf.getId()));
            if (file == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                throw new RuntimeException();
            }
            String loadfile =
                storageService.loadBase64Info(Integer.parseInt(conf.getId()));
            loadFileInfo loadinfo =
                new Gson().fromJson(loadfile, loadFileInfo.class);
            float signx, signy;
            signx = (loadinfo.getwidth() - conf.getx() - 80) /
                    loadinfo.getwidth() * 50000;
            signy = conf.gety() / loadinfo.getheight() * 50000;
            String position = String.format(
                ",\"position\":{\"page\":\"1\",\"x\":\"%f\",\"y\":\"%f\"}",
                signx, signy);
            String doc_no = conf.getFileName();
            try {
                byte[] bytes = doc_no.getBytes("UTF-8");
                doc_no = Base64.getEncoder().encodeToString(bytes);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String doc_data = String.format(
                "{\"document_no\" : \"%s\",\"pdf\":\"%s\",\"type\":\"position\",\"position\":{\"page\":1,\"x\":\"%f\",\"y\":\"%f\"}}",
                conf.getFileType() + "_" + conf.getId() + "_" +
                    doc_no,
                loadinfo.getPdf(), signx, signy);
            if (flg == 0){
                fileData = fileData + doc_data;
                flg = 1;
            }
            else{
                fileData = fileData + "," + doc_data;
            }
        }
        fileData = fileData + "]";

        String data = String.format(
            "{\"api_key\" : \"%s\",\"api_secret\" : \"%s\",\"return_url\":\"%s\",\"redirect_url\":\"%s\",\"seal\": %s}",
            uploadWatcher.getApi_key(), uploadWatcher.getApi_secret(),
            "http://" + host + "/updown/batchSigned",
            redirect_url, fileData);
        try {
            URI uri = new URIBuilder()
                          .setScheme("http")
                          .setHost(uploadWatcher.getHost())
                          .setPort(uploadWatcher.getSignPort())
                          .setPath("/seal/userMoreSign")
                          .build();
            StringEntity requestEntity =
                new StringEntity(data, ContentType.APPLICATION_JSON);
            HttpPost httppost = new HttpPost(uri);
            httppost.setEntity(requestEntity);
            CloseableHttpResponse httpresponse =
                this.httpclient.execute(httppost);
            HttpEntity entity = httpresponse.getEntity();
            int code = httpresponse.getStatusLine().getStatusCode();
            if (entity != null) {
                String jsonStr = EntityUtils.toString(entity);
                logger.debug(jsonStr);
                httpresponse.close();
                if (code == HttpServletResponse.SC_OK) {
                    signRet resp = new Gson().fromJson(jsonStr, signRet.class);
                    return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE,
                                "application/octet-stream")
                        .body(resp.getSign_url());
                }
            } else {
                logger.error("Sign error {}, no result returned.", code);
            }
        } catch (URISyntaxException e) {
            logger.error(e.toString());
        } catch (ClientProtocolException e) {
            logger.error(e.toString());
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
            .body("fail");
    }

    /**
     * Fetch the signed file from CA server.
     *
     */
    @PostMapping("signed")
    public ResponseEntity<String> signed(@RequestBody String jsonStr,
                                         HttpServletResponse response) {
        getSignedFile resp = new Gson().fromJson(jsonStr, getSignedFile.class);
        String doc_no = resp.getdoc_no();
        String pdf = resp.getpdf();
        logger.info("getting signed file {}", doc_no);
        int p1 = 0;
        int p2 = doc_no.indexOf("_", p1);
        String fileType = doc_no.substring(p1, p2);
        p1 = p2 + 1;
        p2 = doc_no.indexOf("_", p1);
        String fileId = doc_no.substring(p1, p2);
        String filename = doc_no.substring(p2 + 1);
        byte[] decoded = null;
        try {
            byte[] bytes = filename.getBytes("UTF-8");
            decoded = Base64.getDecoder().decode(bytes);
        } catch (Exception e) {
            e.printStackTrace();            
        }
        filename = new String(decoded, "UTF-8");
        long timestamp = System.currentTimeMillis();
        String base_dir = UPLOAD_DIR;
        int rnd = this.random.nextInt();
        String filepath =
            new File(base_dir, timestamp + "_" + rnd + "_" + filename)
                .getAbsolutePath();
        byte[] decoded = Base64.getDecoder().decode(pdf);
        File writefile = new File(filepath);
        try {
            FileOutputStream fos = new FileOutputStream(writefile);
            try {
                fos.write(decoded);
                fos.close();
            } catch (IOException e) {
                response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throw new RuntimeException("Could not initialize storage!");
            }
        } catch (FileNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error(e.toString());
            throw new RuntimeException("failed to open file");
        }
        String filestorename = timestamp + "_" + rnd + "_" + filename;
        UploadFile uploadFile =
            this.iDao.saveFile(timestamp, filename, filestorename, writefile.length());
        logger.info("saving file {}.", filename);
        FileInfo fileInfo = new Gson().fromJson("{\"token\":\"token\"}", FileInfo.class);
        UploadConfirmDto uploadConfirmDto =
            new UploadConfirmDto(fileInfo, uploadFile.getId());
        logger.info("confirming upload new signed fileid {}", uploadFile.getId());
        feedWatcher(uploadConfirmDto, fileInfo.getToken(), Long.parseLong(fileId),
                    uploadFile.getId(), Integer.parseInt(fileType));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
            .body("success");
    }

    /**
     * Fetch batch of signed files from CA server.
     *
     */
    @PostMapping("batchSigned")
    public ResponseEntity<String> batchSigned(@RequestBody String jsonStr,
                                         HttpServletResponse response) {
        getBatchSignedFile resp = new Gson().fromJson(jsonStr, getBatchSignedFile.class);
        for (getBatchSignedFile.signPdf content : resp.getPdfList()) {
            String doc_no = content.getdoc_no();
            String pdf = content.get_pdf();
            logger.info("getting signed file {}", doc_no);
            int p1 = 0;
            int p2 = doc_no.indexOf("_", p1);
            String fileType = doc_no.substring(p1, p2);
            p1 = p2 + 1;
            p2 = doc_no.indexOf("_", p1);
            String fileId = doc_no.substring(p1, p2);
            String filename = doc_no.substring(p2 + 1);
            byte[] decoded = null;
            try {
                byte[] bytes = filename.getBytes("UTF-8");
                decoded = Base64.getDecoder().decode(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            filename = new String(decoded, "UTF-8");
            long timestamp = System.currentTimeMillis();
            String base_dir = UPLOAD_DIR;
            int rnd = this.random.nextInt();
            String filepath =
                new File(base_dir, timestamp + "_" + rnd + "_" + filename)
                    .getAbsolutePath();
            byte[] decoded = Base64.getDecoder().decode(pdf);
            File writefile = new File(filepath);
            try {
                FileOutputStream fos = new FileOutputStream(writefile);
                try {
                    fos.write(decoded);
                    fos.close();
                } catch (IOException e) {
                    response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    throw new RuntimeException("Could not initialize storage!");
                }
            } catch (FileNotFoundException e) {
                response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                logger.error(e.toString());
                throw new RuntimeException("failed to open file");
            }
            String filestorename = timestamp + "_" + rnd + "_" + filename;
            UploadFile uploadFile = this.iDao.saveFile(
                timestamp, filename, filestorename, writefile.length());
            logger.info("saving file {}.", filename);
            FileInfo fileInfo =
                new Gson().fromJson("{\"token\":\"token\"}", FileInfo.class);
            UploadConfirmDto uploadConfirmDto =
                new UploadConfirmDto(fileInfo, uploadFile.getId());
            logger.info("confirming upload new signed fileid {}",
                        uploadFile.getId());
            feedWatcher(uploadConfirmDto, fileInfo.getToken(),
                        Long.parseLong(fileId), uploadFile.getId(),
                        Integer.parseInt(fileType));
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
            .body("success");
    }

    /**
     * Notify the watcher that a file was signed just now.
     *
     * @param uploadConfirmDto
     * @return
     */
    private boolean feedWatcher(UploadConfirmDto uploadConfirmDto, String token,
                                long originid, long fguid, int filetype) {
        String jsonContent = String.format("{\"token\":\"%s\",\"originid\":%d,\"fguid\":%d,\"filetype\": %d}", token, originid, fguid, filetype);
        logger.debug("File confirming json string: " + jsonContent);
        try {
            URI uri = new URIBuilder()
                          .setScheme(uploadWatcher.getScheme())
                          .setHost(uploadWatcher.getHost())
                          .setPort(uploadWatcher.getPort())
                          .setPath("/update/updateSignFile")
                          .build();
            logger.debug("File confirming request URI: " + uri.toASCIIString());
            StringEntity requestEntity =
                new StringEntity(jsonContent, ContentType.APPLICATION_JSON);
            HttpPost httppost = new HttpPost(uri);
            httppost.setEntity(requestEntity);
            TimeConsume timeConsume = new TimeConsume();
            CloseableHttpResponse httpresponse =
                this.httpclient.execute(httppost);
            HttpEntity entity = httpresponse.getEntity();
            int code = httpresponse.getStatusLine().getStatusCode();
            if (entity != null) {
                String jsonStr = EntityUtils.toString(entity);
                if (jsonStr == null) {
                    logger.error(
                        "Upload confirm fail!\nResponse body is null!");
                    return false;
                }
                httpresponse.close();
                if (code == HttpServletResponse.SC_OK) {
                    String resp =
                        new Gson()
                            .fromJson(jsonStr, UploadConfirmSuccess.class)
                            .getCode();
                    if (resp.equals("201")) {
                        logger.error(
                            "Upload confirm fail!\n Response body = {}", jsonStr);
                        return false;
                    }
                } else {
                    UploadConfirmFail uploadConfirmFail =
                        new Gson().fromJson(jsonStr, UploadConfirmFail.class);
                    logger.error(
                        "Upload confirm fail!\nResponse token = {}, message = {}",
                        uploadConfirmFail.getToken(),
                        uploadConfirmFail.getMsg());
                }
            } else {
                logger.error(
                    "Upload confirm fail!\nCode = {}, No token returned.",
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
     * Notify the watcher that a file was uploaded just now.
     *
     * @param uploadConfirmDto
     * @return
     */
    private String feedWatcher(UploadConfirmDto uploadConfirmDto) {
        /*
        Build the json parameter to transfer
         */
        logger.debug("check upload");
        String jsonContent = new Gson().toJson(uploadConfirmDto);
        logger.debug("File confirming json string: " + jsonContent);
        try {
            URI uri = new URIBuilder()
                    .setScheme(uploadWatcher.getScheme())
                    .setHost(uploadWatcher.getHost())
                    .setPort(uploadWatcher.getPort())
                    .setPath(uploadWatcher.getPath()+"postOneFileInfo")
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
                logger.debug(jsonStr);
                if (jsonStr == null) {
                    logger.error("Upload confirm fail!\nResponse body is null!");
                    return "connection fail";
                }
                httpresponse.close();
                UploadConfirmSuccess retMsg =
                    new Gson().fromJson(jsonStr, UploadConfirmSuccess.class);
                if (retMsg.getCode().equals("200")) {
                    String respToken = retMsg.getToken();
                    if (respToken == null) {
                        logger.error("Upload confirm fail!\nExpected token = {}, response token is null! Response body = {}",
                                uploadConfirmDto.getFileInfo().getToken(), jsonStr);
                        return "fail";
                    }
                    if (respToken.equals(uploadConfirmDto.getFileInfo().getToken())) {
                        logger.info("Confirmation time cost: {} millis.", timeConsume.getTimeConsume());
                        return "succ";
                    } else{
                        logger.error("Upload confirm fail!\nExpected token = {}, response token = {}",
                                uploadConfirmDto.getFileInfo().getToken(), respToken);
                        return "token error";
                    }
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
        return "fail";
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
