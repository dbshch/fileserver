package uni.akilis.file_server.controller;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uni.akilis.file_server.dao.IDao;
import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.dto.FileRecordDto;
import uni.akilis.file_server.service.StorageService;
import uni.akilis.file_server.util.Consts;

/**
 * Created by leo on 12/24/17.
 * <br/>
 * Upload file by multipart technology. Also show uploaded file list and provide downloading.
 */

@RestController
@RequestMapping(Consts.UP_DOWN_PATH)
public class RestUpDownController {

    @Autowired
    StorageService storageService;

    @Autowired
    private IDao iDao;


    // Multiple file upload
    @Deprecated
    @PostMapping("api/uploadfile")
    public String uploadFileMulti(@RequestParam("file") MultipartFile file, @RequestParam("fileInfo") String fileInfoStr) {
        try {
            FileInfo fileInfo = new Gson().fromJson(fileInfoStr, FileInfo.class);
            String filename = storageService.store(file, fileInfo);
            return "You successfully uploaded - " + filename;
        } catch (JsonSyntaxException e) {
            return e.toString();
        } catch (Exception e) {
            return "FAIL! Maybe You had uploaded the file's size > 100MB";
        }
    }

    @GetMapping("getallfiles")
    public List<FileRecordDto> getListFiles() {
        List<FileRecordDto> lstFiles = this.iDao.findAllFiles();
        return lstFiles;
    }

    @GetMapping("files/{fileId:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable int fileId) {
        Resource file = storageService.loadFile(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
