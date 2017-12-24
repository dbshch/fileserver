package uni.akilis.file_server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by leo on 12/24/17.
 */
@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "upload";
    }

}
