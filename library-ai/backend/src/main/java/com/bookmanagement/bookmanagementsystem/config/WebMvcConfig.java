package com.bookmanagement.bookmanagementsystem.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Make files under <uploadDir> available at /<uploadDir>/**
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        String resourceLocation = "file:" + uploadPath.toString() + "/";

        registry.addResourceHandler("/" + uploadDir + "/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600); // cache for 1 hour (adjust as needed)
    }
}
 