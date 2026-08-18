package com.king.aicustomerservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 将本地上传目录映射为可通过浏览器访问的静态路径
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.path}")
    private String uploadPath;

    /**
     * 配置静态资源映射：/uploads/** -> D:/workspace/uploads/
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadPath.replace("\\", "/");
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}
