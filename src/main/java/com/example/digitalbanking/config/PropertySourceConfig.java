package com.example.digitalbanking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:swagger.properties")
public class PropertySourceConfig {
    // This class enables loading the swagger.properties file
}
