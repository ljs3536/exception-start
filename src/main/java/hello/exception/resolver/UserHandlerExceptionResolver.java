package hello.exception.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.exception.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UserHandlerExceptionResolver implements HandlerExceptionResolver {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception ex) {
        try{
            if(ex instanceof UserException) {
                log.info("UserException resolver to 400");
                String acceptHeader = httpServletRequest.getHeader("accept");
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                if ("application/json".equals(acceptHeader)){
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("ex", ex.getClass());
                    errorResult.put("message", ex.getMessage());

                    String result = objectMapper.writeValueAsString(errorResult);

                    httpServletResponse.setContentType("application/json");
                    httpServletResponse.setCharacterEncoding("utf-8");
                    httpServletResponse.getWriter().write(result);
                    return new ModelAndView();
                }else{
                    return new ModelAndView("error/500");
                }
            }
        }catch(IOException e){
            log.error("resolver ex",e);
        }
        return null;
    }
}
