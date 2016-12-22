package com.github.serezhka.mzdlink;

import com.github.serezhka.mzdlink.config.NioApplicationConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class Application {

    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(NioApplicationConfig.class);
    }
}
