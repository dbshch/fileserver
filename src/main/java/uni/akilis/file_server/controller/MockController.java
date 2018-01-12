package uni.akilis.file_server.controller;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uni.akilis.file_server.dto.UploadConfirmDto;
import uni.akilis.file_server.dto.UploadConfirmFail;
import uni.akilis.file_server.dto.UploadConfirmSuccess;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/mock/")
public class MockController {

private static final Logger logger = LoggerFactory.getLogger(MockController.class);
    @Value("mock-watcher")
    private int mockWatcher = 0;

    @PostMapping("watcher")
    public void mockUploadWatcherResponse(@RequestBody UploadConfirmDto uploadConfirmDto,  HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        int code = HttpServletResponse.SC_BAD_REQUEST;
        String jsonContent = "";
        switch (mockWatcher) {
            case 1:
                code = HttpServletResponse.SC_OK;
                jsonContent = gson.toJson(new UploadConfirmSuccess(uploadConfirmDto.getToken()));
                break;
            case 2:
                code = HttpServletResponse.SC_BAD_REQUEST;
                jsonContent = gson.toJson(new UploadConfirmFail(uploadConfirmDto.getToken(),
                        "Token is denied by server."));
                break;
            case 3:
                code = code = HttpServletResponse.SC_OK;
                jsonContent = gson.toJson(new UploadConfirmSuccess("fake" + uploadConfirmDto.getToken()));
            default:
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        response.setStatus(code);
        response.getWriter().print(jsonContent);
    }
}
