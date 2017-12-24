package uni.akilis.file_server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uni.akilis.file_server.dao.IDao;
import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.dto.FileRecordDto;
import uni.akilis.file_server.service.StorageService;

/**
 * Created by leo on 12/24/17.
 */

@RestController
public class RestUploadController {

    @Autowired
    StorageService storageService;

    @Autowired
            private IDao iDao;


    // Multiple file upload
    @PostMapping("/api/uploadfile")
    public String uploadFileMulti(@RequestParam("file") MultipartFile file, @RequestParam("fileInfo") FileInfo fileInfo) {
        try {
            String filename = storageService.store(file, fileInfo);
            return "You successfully uploaded - " + filename;
        } catch (Exception e) {
            return "FAIL! Maybe You had uploaded the file before or the file's size > 500KB";
        }
    }

    @GetMapping("/getallfiles")
    public List<FileRecordDto> getListFiles() {
        List<FileRecordDto> lstFiles = this.iDao.findAllFiles();
        return lstFiles;
    }

    @GetMapping("/files/{fileId:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable long fileId) {
        Resource file = storageService.loadFile(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
