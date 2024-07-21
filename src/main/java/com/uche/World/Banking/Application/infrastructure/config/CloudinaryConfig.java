package com.uche.World.Banking.Application.infrastructure.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private final String CLOUD_NAME = "da99lsxaa";

    private final String API_KEY = "213996242223836";

    private final String API_SECRET = "FAtQTKcJItlbT8jczpV4n3Mb7AU";



    @Bean
    public Cloudinary cloudinary(){
        Map<String, String> config = new HashMap<>();


        config.put("cloud_name", CLOUD_NAME);
        config.put("api_key", API_KEY);
        config.put("api_secret", API_SECRET);

        return new Cloudinary(config);
    }
}
