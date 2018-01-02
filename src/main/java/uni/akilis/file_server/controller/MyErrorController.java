package uni.akilis.file_server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by leo on 10/27/17.
 * <br/>
 * Handle kinds of exceptions.
 */
@Controller
public class MyErrorController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(MyErrorController.class);

    private final String PATH = "/error";
    @Override
    public String getErrorPath() {
        return PATH;
    }

    @RequestMapping(value = PATH)
    @ResponseBody
    public String errorHandling(HttpServletResponse response) {
        String ans = "";
        switch (response.getStatus()) {
            case 404: {
                ans = "404 Something amazing is happening! Please be patient.";
                break;
            }
            case 500: {
                ans = "500 Sorry. We are fixing this problem now for you. Please review it later. :)";
                break;
            }
            default:
                ans = "Unknown error! Please try it later.";
        }
        return ans;
    }
}
