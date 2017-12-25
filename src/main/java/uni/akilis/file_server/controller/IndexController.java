package uni.akilis.file_server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import uni.akilis.file_server.util.Consts;

/**
 * Created by leo on 12/24/17.
 */
@Controller
public class IndexController {

    @GetMapping(value = {"/", Consts.UP_DOWN_PATH})
    public String index() {
        return "up_down";
    }

}
