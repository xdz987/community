package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

// 扫描范围为带有@Controller的Bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // 处理Exception的异常(因为Exception为所有异常的父类)，不过不够精细
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发送异常"+e.getMessage());
        for (StackTraceElement element:e.getStackTrace()){
            logger.error(element.toString());
        }

//        Map<String, String> map = new HashMap<String, String>();
//        Enumeration enumeration = request.getHeaderNames();
//        while(enumeration.hasMoreElements()){
//            String key = (String) enumeration.nextElement();
//            String value = request.getHeader(key);
//            map.put(key, value);
//        }
//        System.out.println(map.toString());

        String xRequestedWith = request.getHeader("x-requested-with");
        // 说明是ajax异步请求
        if("XMLHttpRequest".equals(xRequestedWith)){
            // plain为普通字符串，json为json字符串且Spring会自动转换为json格式
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        }else{
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }
}
