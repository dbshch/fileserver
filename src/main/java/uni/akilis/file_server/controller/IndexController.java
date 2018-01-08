package uni.akilis.file_server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import uni.akilis.file_server.util.Consts;

/**
 * Created by leo on 12/24/17.
 * <br/>
 * Home page for accessing file server.
 */
@Controller
public class IndexController {

    @GetMapping(value = {"/"})
    public String index() {
        return "up_down";
    }

}
