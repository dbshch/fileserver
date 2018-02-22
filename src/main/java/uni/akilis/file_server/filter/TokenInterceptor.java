package uni.akilis.file_server.filter;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uni.akilis.file_server.dto.FileInfo;
import uni.akilis.file_server.util.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Validate the access with token.
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);
    @Value("${token}")
    private String token;


    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String fileInfoStr = httpServletRequest.getParameter("fileInfo");
        if (httpServletRequest.getMethod().equals("OPTIONS")){
            return true;
        }
        if (fileInfoStr != null) {
            FileInfo fileInfo = new Gson().fromJson(fileInfoStr, FileInfo.class);
            if (this.token.equals(fileInfo.getToken())) {
                return true;
            }
        }
        else{
            // TODO: wait for checking reason of leaking token
            // String tempInfo = "{\"token\":\"token\"}";
            // FileInfo fileInfo =
            //     new Gson().fromJson(tempInfo, FileInfo.class);
            return true;
        }
        httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        logger.warn("Token error!\nRemote address = {}\nmethod = {}\nURI = {}\nparameters = {}",
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI(),
                Utils.mapToString(httpServletRequest.getParameterMap()));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
