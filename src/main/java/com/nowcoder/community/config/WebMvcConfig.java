package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    //注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 不拦截静态资源，拦截register login等
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/*/*.css","/*/*.js","/*/*.png","/*/*.jpg","/*/*.jpeg")
                .addPathPatterns("/register","/login");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/*/*.css","/*/*.js","/*/*.png","/*/*.jpg","/*/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/*/*.css","/*/*.js","/*/*.png","/*/*.jpg","/*/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/*/*.css","/*/*.js","/*/*.png","/*/*.jpg","/*/*.jpeg");

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/*/*.css","/*/*.js","/*/*.png","/*/*.jpg","/*/*.jpeg");
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/community/**").addResourceLocations("classpath:/community/");
//    }
}
