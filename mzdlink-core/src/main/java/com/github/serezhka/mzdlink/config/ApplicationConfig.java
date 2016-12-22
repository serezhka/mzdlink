package com.github.serezhka.mzdlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Configuration
@ComponentScan("com.github.serezhka.mzdlink")
@PropertySource("classpath:mzdlink.properties")
public class ApplicationConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
